package de.jadehs.vcg

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.StrictMode
import android.os.strictmode.Violation
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import de.jadehs.vcg.R.string.nearby_channel_description
import de.jadehs.vcg.broadcast_receiver.TrophyBroadcastReceiver
import de.jadehs.vcg.services.NearbyWaypointService
import de.jadehs.vcg.services.audio.AudioPlayerService
import de.jadehs.vcg.utils.data.FileProvider
import de.jadehs.vcg.utils.data.FileUtils
import org.oscim.utils.Parameters
import java.util.*


private const val TAG = "MyApplication"
const val MEDIA_FILES_PATH = "media"
const val MAPS_FILES_PATH = "maps"

class MyApplication : Application() {


    override fun onCreate() {


        if (BuildConfig.DEBUG) {
            val threadPolicy = StrictMode.ThreadPolicy.Builder().detectNetwork()
                    .detectResourceMismatches()
                    .penaltyDeath()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                threadPolicy.penaltyListener(mainExecutor, StrictMode.OnThreadViolationListener { violation: Violation? ->
                    violation?.printStackTrace()
                })
            }
            StrictMode.setThreadPolicy(threadPolicy.build())
            val vmPolicy = StrictMode.VmPolicy.Builder()
                    .detectLeakedClosableObjects()
                    .detectActivityLeaks()
                    .detectCleartextNetwork()
                    .detectLeakedRegistrationObjects()
                    .detectLeakedSqlLiteObjects()
                    .penaltyLog()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                vmPolicy.penaltyListener(mainExecutor, StrictMode.OnVmViolationListener { violation: Violation? ->
                    violation?.printStackTrace()
                })
            }
            StrictMode.setVmPolicy(vmPolicy.build())
        }
        super.onCreate()


        Parameters.MARKER_SORT = false;

        //load assets
        copyAssetsIfNeeded()
        createNotiChannels()


    }

    private fun registerPlaybackLocationUpdates() {
        val manager = LocalBroadcastManager.getInstance(this.applicationContext);
        manager.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d(TAG, "onReceive: " + intent?.extras.toString())
            }
        }, IntentFilter(AudioPlayerService.PLAYBACK_LOCATION_CHANGED_ACTION))
    }


    private fun copyAssetsIfNeeded() {
        val fileProvider = FileProvider(this.applicationContext)
        val outputDir = fileProvider.mediaRoot
        FileUtils.copyAllFilesFromAssetsIfNotExists(this.applicationContext, MEDIA_FILES_PATH, outputDir)

        val outputDirMap = fileProvider.mapsRoot
        FileUtils.copyAllFilesFromAssetsIfNotExists(this.applicationContext, MAPS_FILES_PATH, outputDirMap)
        Log.d(TAG, "copyAssetsIfNeeded: " + Arrays.deepToString(outputDirMap.list()))
    }


    private fun createNotiChannels() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = getString(R.string.nearby_channel_name)
            val description = getString(nearby_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NearbyWaypointService.NOTIF_ID, name, importance).apply {
                this.description = description
            }

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = getString(R.string.trophy_channel_name)
            val description = getString(R.string.trophy_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(TrophyBroadcastReceiver.NOTIFICATION_CHANNEL, name, importance).apply {
                this.description = description
            }

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }

    }
}