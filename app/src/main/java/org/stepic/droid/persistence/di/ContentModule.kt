package org.stepic.droid.persistence.di

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import org.stepic.droid.persistence.content.StepContentResolver
import org.stepic.droid.persistence.content.StepContentResolverImpl
import org.stepic.droid.persistence.content.processors.ImageStepContentProcessor
import org.stepic.droid.persistence.content.processors.StepContentProcessor
import org.stepic.droid.persistence.content.processors.VideoStepContentProcessor

@Module
interface ContentModule {

    @Binds
    @IntoSet
    fun bindVideoStepContentProcessor(videoStepContentProcessor: VideoStepContentProcessor): StepContentProcessor

    @Binds
    @IntoSet
    fun bindImageStepContentProcessor(imageStepContentProcessor: ImageStepContentProcessor): StepContentProcessor

    @Binds
    @PersistenceScope
    fun bindStepContentResolver(stepContentResolverImpl: StepContentResolverImpl): StepContentResolver

}