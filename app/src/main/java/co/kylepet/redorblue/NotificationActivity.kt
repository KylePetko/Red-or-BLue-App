package co.kylepet.redorblue

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.Menu
import com.google.firebase.analytics.FirebaseAnalytics
import android.content.Intent
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar;
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat


class NotificationActivity : AppCompatActivity() {

    private val dateFrag = DateFragment()
    private val settingsFrag = SettingsFragment()
    private lateinit var firebaseAnalytics: FirebaseAnalytics





    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {


        var myToolbar = findViewById<Toolbar>(R.id.action_bar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        //Android less than O doesn't support dark navbar icons, so just make the navbar dark
        if ((Build.VERSION.SDK_INT <= Build.VERSION_CODES.O)) {
            window.navigationBarColor = ContextCompat.getColor(this, R.color.black)
        }

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_notification_settings)

    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            android.R.id.home -> {
                //val homeIntent = Intent(this, MainActivity::class.java)
                //startActivity(homeIntent)

                finish()
            }
        }
        return super.onOptionsItemSelected(menuItem)
    }


}