package cz.petrf.kvalitaacena

import android.app.Application

class KvalitaACenaApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    AppContainer.init(this)
  }
}
