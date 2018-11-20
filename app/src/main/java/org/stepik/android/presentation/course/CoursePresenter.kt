package org.stepik.android.presentation.course

import android.os.Bundle
import io.reactivex.*
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.CourseId
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.course.interactor.CourseEnrollmentInteractor
import org.stepik.android.domain.course.interactor.CourseInteractor
import org.stepik.android.domain.course.model.CourseHeaderData
import org.stepik.android.domain.course.model.EnrollmentState
import org.stepik.android.model.Course
import org.stepik.android.presentation.base.PresenterBase
import org.stepik.android.view.injection.course.EnrollmentCourseUpdates
import javax.inject.Inject

class CoursePresenter
@Inject
constructor(
    @CourseId
    private val courseId: Long,

    private val courseInteractor: CourseInteractor,
    private val courseEnrollmentInteractor: CourseEnrollmentInteractor,

    @EnrollmentCourseUpdates
    private val enrollmentUpdatesObservable: Observable<Long>,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<CourseView>() {
    companion object {
        private const val KEY_COURSE_HEADER_DATA = "course_header_data"
    }

    private var state: CourseView.State = CourseView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    init {
        subscriberForEnrollmentUpdates()
    }

    override fun attachView(view: CourseView) {
        super.attachView(view)
        view.setState(state)
    }

    /**
     * Data initialization variants
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        if (state != CourseView.State.Idle) return
        val data = savedInstanceState.getParcelable(KEY_COURSE_HEADER_DATA)
                as? CourseHeaderData ?: return
        state = CourseView.State.CourseLoaded(data)
    }

    fun onCourseId(courseId: Long) {
        observeCourseData(courseInteractor.getCourseHeaderData(courseId))
    }

    fun onCourse(course: Course) {
        observeCourseData(courseInteractor.getCourseHeaderData(course))
    }

    private fun observeCourseData(courseDataSource: Maybe<CourseHeaderData>) {
        if (state != CourseView.State.Idle) return
        state = CourseView.State.Loading
        compositeDisposable += courseDataSource
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onComplete = { state = CourseView.State.EmptyCourse },
                onSuccess  = { state = CourseView.State.CourseLoaded(it) },
                onError    = { state = CourseView.State.NetworkError }
            )
    }

    /**
     * ENROLLMENT
     */
    fun enrollCourse() {
        toggleEnrollment(CourseEnrollmentInteractor::enrollCourse)
    }

    fun dropCourse() {
        toggleEnrollment(CourseEnrollmentInteractor::dropCourse)
    }

    private inline fun toggleEnrollment(enrollmentAction: CourseEnrollmentInteractor.(Long) -> Completable) {
        val headerData = (state as? CourseView.State.CourseLoaded)
            ?.courseHeaderData
            ?.takeIf { it.enrollmentState != EnrollmentState.PENDING }
            ?: return

        state = CourseView.State.CourseLoaded(headerData.copy(enrollmentState = EnrollmentState.PENDING))
        compositeDisposable += courseEnrollmentInteractor
            .enrollmentAction(headerData.courseId)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onError = {
                    view?.showEnrollmentError()
                    state = CourseView.State.CourseLoaded(headerData) // roll back data
                }
            )
    }

    private fun subscriberForEnrollmentUpdates() {
        compositeDisposable += enrollmentUpdatesObservable
            .filter { it == courseId }
            .concatMap { courseInteractor.getCourseHeaderData(it, canUseCache = false).toObservable() }
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext  = { state = CourseView.State.CourseLoaded(it) },
                onError = { state = CourseView.State.NetworkError; subscriberForEnrollmentUpdates() }
            )
    }

    /**
     * Continue learning
     */
    fun continueLearning() {

    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(KEY_COURSE_HEADER_DATA, (state as? CourseView.State.CourseLoaded)?.courseHeaderData)
    }
}