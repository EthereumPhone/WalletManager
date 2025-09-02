package com.core.database

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.core.database.util.GeneratePrePopulatedDatabase
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test to generate a pre-populated database.
 * 
 * Run this test once to generate the database asset file:
 * ./gradlew :core:database:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.core.database.GeneratePrePopulatedDatabaseTest
 * 
 * After running, pull the database from the device:
 * adb pull /data/data/[your.package]/files/prepopulated_wm.db
 * 
 * Then copy it to: app/src/main/assets/database/wm_database.db
 */
@RunWith(AndroidJUnit4::class)
class GeneratePrePopulatedDatabaseTest {
    
    @Test
    fun generatePrePopulatedDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        
        // Generate the pre-populated database
        GeneratePrePopulatedDatabase.generate(context)
        
        // The database is now saved to the device's files directory
        // Follow the console output instructions to retrieve it
    }
}
