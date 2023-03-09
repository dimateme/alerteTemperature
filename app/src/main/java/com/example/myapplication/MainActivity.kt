package com.example.myapplication

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.mqtt.MqttClientHelper
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.content_main.*


import java.util.*
import kotlin.concurrent.schedule


class MainActivity : AppCompatActivity() {
    lateinit var viewModel: MainActivityViewModel
    var temperatureObtenue:Float = 0.0f
    lateinit var afficherValeurTemperature: TextView
    lateinit var editTextDate: EditText
    var str:String = "0"
    var temperaturePrecedente:Float = 0.0f
    private val mqttClient by lazy {
        MqttClientHelper(this)

    }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        setMqttCallBack()
        afficherValeurTemperature = findViewById(R.id.temperatureDhtt22Value)


        if(mqttClient.isConnected()) {
            mqttClient.subscribe("topic_pub")
        }else{
            Timer("SettingUp", false).schedule(1000) {
               mqttClient.subscribe("topic_pub")

            }

        }




    }


    private fun setMqttCallBack() {
          fun initRecyclerView(){
            viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
            viewModel.getCreateNewTemperatureObserver().observe(this, Observer<TemperatureResponse?> {
                if(it != null){
                    Toast.makeText(this@MainActivity, "Temperature ajoutée", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this@MainActivity, "Erreur", Toast.LENGTH_SHORT).show()
                }
            })

        }

        mqttClient.setCallback(object : MqttCallbackExtended {
            override fun connectionLost(throwable: Throwable) {
                val snackbarMsg = "Connection to host lost:\n'$SOLACE_MQTT_HOST'"
                Log.w("Debug", snackbarMsg)
                Snackbar.make(findViewById(android.R.id.content), snackbarMsg, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            }
            override fun connectComplete(b: Boolean, s: String) {
                val snackbarMsg = "Connected to host:\n'$SOLACE_MQTT_HOST'."
                Log.w("Debug", snackbarMsg)
                Snackbar.make(findViewById(android.R.id.content), snackbarMsg, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            }

            @Throws(Exception::class)
            override fun messageArrived(topic: String, mqttMessage: MqttMessage) {
                initRecyclerView()
                val calendar: Calendar = Calendar.getInstance()
                val currentDateTime: String = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)+1}-${calendar.get(Calendar.DAY_OF_MONTH)} ${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)}:${calendar.get(Calendar.SECOND)}"

                Log.w("Debug", "Message received from host '$SOLACE_MQTT_HOST': $mqttMessage")
                str = mqttMessage.toString()
                afficherValeurTemperature.text = str
                val valeur = str.toFloat()

                //Methode qui permetd d'ajouter la temperature chaque 6 minutes
                Timer("SettingUp", false).schedule(360000) {
                    if(valeur != temperaturePrecedente){
                        val temperature = Temperature(valeur.toFloat(),currentDateTime)
                        viewModel.createNewTemperature(temperature)

                    }
                    temperaturePrecedente = valeur
                }


                editTextDate.setText(temperature.toString())


                temperatureObtenue = afficherValeurTemperature.text.toString().toFloat()


            }

            override fun deliveryComplete(iMqttDeliveryToken: IMqttDeliveryToken) {
                Log.w("Debug", "Message published to host '$SOLACE_MQTT_HOST'")
            }

        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Gérer les clics sur les éléments de la barre d'action ici. La barre d'action
        // gère automatiquement les clics sur le bouton Home/Up, aussi longtemps
        // que vous spécifiez une activité parente dans AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        mqttClient.destroy()
        super.onDestroy()
    }



}
