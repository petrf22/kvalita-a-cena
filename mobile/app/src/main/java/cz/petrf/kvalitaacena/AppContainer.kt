package cz.petrf.kvalitaacena

import android.content.Context
import cz.petrf.kvalitaacena.auth.AuthRepository
import cz.petrf.kvalitaacena.network.GraphQlClient

/** Ruční DI bez Hiltu/Daggeru — appka je malá, jeden container stačí. */
object AppContainer {
  lateinit var authRepository: AuthRepository
    private set

  lateinit var graphQlClient: GraphQlClient
    private set

  fun init(context: Context) {
    if (::authRepository.isInitialized) return
    authRepository = AuthRepository(context.applicationContext)
    graphQlClient = GraphQlClient(authRepository)
  }
}
