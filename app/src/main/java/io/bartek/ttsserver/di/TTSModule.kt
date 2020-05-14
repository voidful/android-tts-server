package io.bartek.ttsserver.di

import android.content.Context
import android.content.SharedPreferences
import android.speech.tts.TextToSpeech
import androidx.preference.PreferenceManager
import dagger.Module
import dagger.Provides
import io.bartek.ttsserver.network.NetworkUtil
import io.bartek.ttsserver.service.ForegroundNotificationFactory
import io.bartek.ttsserver.sonos.SonosQueue
import io.bartek.ttsserver.tts.TTS
import io.bartek.ttsserver.tts.TTSStatusHolder
import io.bartek.ttsserver.web.WebServerFactory
import javax.inject.Singleton

@Module
class TTSModule {

   @Provides
   @Singleton
   fun ttsStatusHolder() = TTSStatusHolder()

   @Provides
   @Singleton
   fun textToSpeech(context: Context, ttsStatusHolder: TTSStatusHolder) =
      TextToSpeech(context, ttsStatusHolder)

   @Provides
   @Singleton
   fun tts(context: Context, textToSpeech: TextToSpeech, ttsStatusHolder: TTSStatusHolder) =
      TTS(context, textToSpeech, ttsStatusHolder)

   @Provides
   @Singleton
   fun webServerFactory(
      preferences: SharedPreferences,
      context: Context,
      tts: TTS,
      sonos: SonosQueue
   ) =
      WebServerFactory(preferences, context, tts, sonos)

   @Provides
   @Singleton
   fun preferences(context: Context) = PreferenceManager.getDefaultSharedPreferences(context)

   @Provides
   @Singleton
   fun networkUtil(context: Context) = NetworkUtil(context)

   @Provides
   @Singleton
   fun sonosQueue(tts: TTS, networkUtil: NetworkUtil, preferences: SharedPreferences) =
      SonosQueue(tts, networkUtil, preferences)

   @Provides
   @Singleton
   fun foregroundNotificationFactory(context: Context) = ForegroundNotificationFactory(context)
}