package org.stepik.android.view.course.routing

import android.content.Context
import org.stepic.droid.core.ScreenManager
import org.stepik.android.view.routing.deeplink.BranchDeepLinkRouter
import org.stepik.android.view.routing.deeplink.BranchRoute
import javax.inject.Inject

class CourseBranchDeepLinkRouter
@Inject
constructor() : BranchDeepLinkRouter {
    override fun handleBranchRoute(screenManager: ScreenManager, context: Context, route: BranchRoute): Boolean {
        if (route !is CourseScreenBranchRoute) return false

        screenManager.showCourseDescription(context, route.courseId)
        return true
    }
}