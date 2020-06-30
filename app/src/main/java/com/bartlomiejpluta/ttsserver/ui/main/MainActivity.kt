package com.bartlomiejpluta.ttsserver.ui.main

import android.content.*
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bartlomiejpluta.R
import com.bartlomiejpluta.ttsserver.core.util.NetworkUtil
import com.bartlomiejpluta.ttsserver.service.foreground.ForegroundService
import com.bartlomiejpluta.ttsserver.service.state.ServiceState
import com.bartlomiejpluta.ttsserver.ui.help.HelpActivity
import com.bartlomiejpluta.ttsserver.ui.preference.component.PreferencesActivity
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject


class MainActivity : DaggerAppCompatActivity() {
   private lateinit var serverControlButton: AppCompatImageButton
   private lateinit var serverStatus: TextView
   private lateinit var promptText: TextView

   @Inject
   lateinit var context: Context

   @Inject
   lateinit var preferences: SharedPreferences

   @Inject
   lateinit var networkUtil: NetworkUtil

   private val receiver = object : BroadcastReceiver() {
      override fun onReceive(context: Context?, intent: Intent?) {
         (intent?.getStringExtra(ForegroundService.STATE) ?: ServiceState.STOPPED.name)
            .let { ServiceState.valueOf(it) }
            .let { updateViewAccordingToServiceState(it) }
      }
   }

   override fun onCreateOptionsMenu(menu: Menu?): Boolean {
      menuInflater.inflate(R.menu.menu_main, menu)
      return true
   }

   override fun onOptionsItemSelected(item: MenuItem): Boolean {
      when (item.itemId) {
         R.id.open_preferences -> startActivity(Intent(this, PreferencesActivity::class.java))
         R.id.open_help -> startActivity(Intent(this, HelpActivity::class.java))
      }

      return super.onOptionsItemSelected(item)
   }

   private fun updateViewAccordingToServiceState(newState: ServiceState) {
      serverControlButton.isEnabled = true
      when (newState) {
         ServiceState.STOPPED -> {
            serverControlButton.setImageResource(R.drawable.ic_power_off)
            serverStatus.text = getString(R.string.main_activity_server_status_down)
            promptText.text = getString(R.string.main_activity_prompt_to_run)
         }
         ServiceState.RUNNING -> {
            serverControlButton.setImageResource(R.drawable.ic_power_on)
            serverStatus.text =
               getString(R.string.main_activity_server_status_up, networkUtil.serverAddress)
            promptText.text = getString(R.string.main_activity_prompt_to_stop)
         }
      }
   }

   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      setContentView(R.layout.activity_main)
      serverControlButton = findViewById(R.id.server_control_button)
      serverStatus = findViewById(R.id.server_status)
      promptText = findViewById(R.id.prompt_text)
   }

   override fun onResume() {
      super.onResume()
      LocalBroadcastManager
         .getInstance(this)
         .registerReceiver(receiver, IntentFilter(ForegroundService.CHANGE_STATE))
      updateViewAccordingToServiceState(ForegroundService.state)
   }

   override fun onPause() {
      LocalBroadcastManager
         .getInstance(this)
         .unregisterReceiver(receiver)
      super.onPause()
   }

   fun controlServer(view: View) {
      serverControlButton.isEnabled = false
      when (ForegroundService.state) {
         ServiceState.STOPPED -> actionOnService(ForegroundService.START)
         ServiceState.RUNNING -> actionOnService(ForegroundService.STOP)
      }
   }

   private fun actionOnService(action: String) {
      Intent(this, ForegroundService::class.java).also {
         it.action = action
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(it)
            return
         }

         startService(it)
      }
   }
}
