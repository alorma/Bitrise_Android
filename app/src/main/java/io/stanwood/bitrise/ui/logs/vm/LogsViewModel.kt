package io.stanwood.bitrise.ui.logs.vm

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.databinding.BaseObservable
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.text.Html
import android.text.Spanned
import io.stanwood.bitrise.data.model.App
import io.stanwood.bitrise.data.model.Build
import io.stanwood.bitrise.data.net.BitriseService
import io.stanwood.bitrise.navigation.SCREEN_ERROR
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import ru.terrakok.cicerone.Router
import timber.log.Timber


class LogsViewModel(
        private val service: BitriseService,
        private val token: String,
        private val router: Router,
        private val app: App,
        private val build: Build) : LifecycleObserver, BaseObservable() {

    val isLoading = ObservableBoolean(false)
    var log = ObservableField<Spanned>()
    private var deferred: Deferred<Any>? = null

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun start() {
        deferred = async(UI) {
            onRefresh()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun stop() {
        deferred?.cancel()
    }

    fun onRefresh() {
        deferred = async(UI) {
            try {
                isLoading.set(true)
                log.apply {
                    val log = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        Html.fromHtml(fetchLog(), Html.FROM_HTML_MODE_COMPACT)
                    } else {
                        Html.fromHtml(fetchLog())
                    }
                    set(log)
                }
            } catch (exception: Exception) {
                Timber.e(exception)
                router.navigateTo(SCREEN_ERROR, exception.message)
            } finally {
                isLoading.set(false)
            }
        }
    }

    private suspend fun fetchLog() =
        service
            .getBuildLog(token, app.slug, build.slug)
            .await()
            .logChunks
            .sortedBy { logChunk -> logChunk.position }
            .joinToString { logChunk -> logChunk.chunk }
}