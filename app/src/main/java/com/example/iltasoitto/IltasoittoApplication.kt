package com.example.iltasoitto

import android.app.Application
import android.util.Log

class IltasoittoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // This is a workaround for a Garbage Collector bug on some Samsung devices.
        // It disables a feature that can cause the app to freeze during audio playback.
        // We use reflection because VMRuntime is a non-public API.
        try {
            // Use reflection to get the VMRuntime class
            val vmRuntimeClass = Class.forName("dalvik.system.VMRuntime")

            // Get the static getRuntime() method
            val getRuntimeMethod = vmRuntimeClass.getMethod("getRuntime")

            // Invoke getRuntime() to get the singleton instance
            val vmRuntimeInstance = getRuntimeMethod.invoke(null)

            // Get the setTargetHeapUtilization(float) method
            val setTargetHeapUtilizationMethod = vmRuntimeClass.getMethod("setTargetHeapUtilization", Float::class.javaPrimitiveType)

            // Invoke the method with the desired value (0.75f)
            setTargetHeapUtilizationMethod.invoke(vmRuntimeInstance, 0.75f)

            Log.d("IltasoittoApplication", "Successfully applied GC workaround.")

        } catch (e: Exception) {
            // Log any errors, this is a best-effort fix.
            Log.e("IltasoittoApplication", "Failed to apply GC workaround", e)
        }
    }
}
