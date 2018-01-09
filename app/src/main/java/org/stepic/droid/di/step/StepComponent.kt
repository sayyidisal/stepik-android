package org.stepic.droid.di.step

import dagger.Subcomponent
import org.stepic.droid.adaptive.model.Card
import org.stepic.droid.base.StepBaseFragment
import org.stepic.droid.di.comment.CommentsComponent
import org.stepic.droid.di.step.code.CodeComponent
import org.stepic.droid.di.streak.StreakModule
import org.stepic.droid.ui.fragments.StepAttemptFragment
import org.stepic.droid.ui.fragments.VideoStepFragment

@StepScope
@Subcomponent(modules = arrayOf(StreakModule::class, CommentCountModule::class))
interface StepComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): StepComponent
    }

    fun commentsComponentBuilder(): CommentsComponent.Builder

    fun codeComponentBuilder(): CodeComponent.Builder

    fun inject(stepFragment: StepBaseFragment)

    fun inject(videoStepFragment: VideoStepFragment)

    fun inject(stepAttemptFragment: StepAttemptFragment)

    fun inject(card: Card)
}
