package io.stanwood.bitrise.ui.newbuild.vm

import android.content.SharedPreferences
import android.content.res.Resources
import android.databinding.BaseObservable
import android.databinding.Bindable
import android.databinding.ObservableBoolean
import io.stanwood.bitrise.R
import io.stanwood.bitrise.data.model.App
import io.stanwood.bitrise.data.model.BuildParams
import io.stanwood.bitrise.data.model.NewBuildParams
import io.stanwood.bitrise.data.net.BitriseService
import io.stanwood.bitrise.di.Properties
import io.stanwood.bitrise.navigation.SCREEN_ERROR
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import ru.terrakok.cicerone.Router
import timber.log.Timber

class NewBuildViewModel(private val resources: Resources,
                        private val router: Router,
                        private val service: BitriseService,
                        private val sharedPreferences: SharedPreferences,
                        private val token: String,
                        private val app: App): BaseObservable() {

    val title: String
        get() = app.title

    @get:Bindable
    var branch: String
        get() = sharedPreferences.getString(Properties.BRANCH, "")
        set(value) = sharedPreferences.edit().putString(Properties.BRANCH, value).apply()

    @get:Bindable
    var workflow: String
        get() = sharedPreferences.getString(Properties.WORKFLOW, "")
        set(value) = sharedPreferences.edit().putString(Properties.WORKFLOW, value).apply()

    val isLoading = ObservableBoolean(false)

    fun onStartNewBuild() {
        async(UI) {
            try {
                isLoading.set(true)
                startNewBuild().let {
                    val message = resources.getString(R.string.new_build_started, it.buildNumber)
                    router.exitWithMessage(message)
                }
            } catch (exception: Exception) {
                Timber.e(exception)
                router.navigateTo(SCREEN_ERROR, exception.message)
            } finally {
                isLoading.set(false)
            }
        }
    }

    private suspend fun startNewBuild() =
        service
            .startNewBuild(token, app.slug, NewBuildParams(BuildParams(branch, workflow)))
            .await()
}