package me.rei_m.hbfavmaterial.di

import dagger.Module
import dagger.Provides
import me.rei_m.hbfavmaterial.presentation.helper.ActivityNavigator

@Module
class ActivityModule {
    @Provides
    fun provideActivityNavigator(): ActivityNavigator {
        return ActivityNavigator()
    }
}
