package com.redix.carnative.car

import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

/**
 * Android for Cars entry point for Redix Auto.
 * Binds to Android Auto host and establishes in-car visual projection session.
 */
class RedixCarAppService : CarAppService() {

    override fun createHostValidator(): HostValidator {
        // Allows connection to all OEM head units and aftermarket car displays
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
    }

    override fun onCreateSession(): Session {
        return RedixCarSession()
    }
}
