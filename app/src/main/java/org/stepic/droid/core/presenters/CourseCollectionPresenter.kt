package org.stepic.droid.core.presenters

import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Singles.zip
import org.stepic.droid.core.presenters.contracts.CoursesView
import org.stepic.droid.di.course_list.CourseListScope
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.features.course_purchases.domain.CoursePurchasesInteractor
import org.stepik.android.model.Course
import org.stepic.droid.model.CourseReviewSummary
import org.stepik.android.model.Progress
import org.stepic.droid.util.CourseUtil
import org.stepic.droid.web.Api
import org.stepic.droid.web.CourseReviewResponse
import org.stepic.droid.web.CoursesMetaResponse
import org.stepic.droid.web.ProgressesResponse
import javax.inject.Inject

@CourseListScope
class CourseCollectionPresenter
@Inject
constructor(
    private val coursePurchasesInteractor: CoursePurchasesInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler,
    private val api: Api
) : PresenterBase<CoursesView>() {

    companion object {
        //collections are small (less than 10 courses), so pagination is not needed
        private const val DEFAULT_PAGE = 1
    }

    private val compositeDisposable = CompositeDisposable()

    fun onShowCollections(courseIds: LongArray) {
        view?.showLoading()
        val disposable = api
            .getCoursesReactive(DEFAULT_PAGE, courseIds)
            .map(CoursesMetaResponse::getCourses)
            .flatMap {
                val progressIds = it.map(Course::progress).toTypedArray()
                val reviewIds = it.map(Course::reviewSummary).toLongArray()

                zip(Single.just(it), getProgressesSingle(progressIds), getReviewsSingle(reviewIds)) { courses, progressMap, reviews ->
                    CourseUtil.applyProgressesToCourses(progressMap, courses)
                    CourseUtil.applyReviewsToCourses(reviews, courses)
                    courses
                }
            }
            .flatMap { courseList ->
                val coursesMap = courseList.associateBy(Course::id)
                val courses = courseIds
                    .asIterable()
                    .mapNotNull(coursesMap::get)

                zip(
                    Single.just(courses),
                    coursePurchasesInteractor.getCoursesSkuMap(courses),
                    coursePurchasesInteractor.getCoursesPaymentsMap(courses)
                )
            }
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribe({ (courses, skus, coursePayments) ->
                view?.showCourses(courses, skus, coursePayments)
            }, {
                view?.showConnectionProblem()
            })

        compositeDisposable.add(disposable)
    }

    private fun getReviewsSingle(reviewIds: LongArray): Single<List<CourseReviewSummary>> {
        return api.getCourseReviews(reviewIds)
                .map(CourseReviewResponse::courseReviewSummaries)
                .subscribeOn(backgroundScheduler)
    }


    private fun getProgressesSingle(progressIds: Array<String?>): Single<Map<String?, Progress>> {
        return api.getProgressesReactive(progressIds)
                .map(ProgressesResponse::getProgresses)
                .map { it.associateBy(Progress::id) }
                .subscribeOn(backgroundScheduler)
    }

    override fun detachView(view: CoursesView) {
        super.detachView(view)
        compositeDisposable.clear()
    }

}
