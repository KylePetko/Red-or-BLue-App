package co.kylepet.redorblue

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Build
import android.text.TextUtils.replace
import android.view.*
import androidx.appcompat.widget.Toolbar;
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.analytics.FirebaseAnalytics


class MainActivity : AppCompatActivity() {

    private val dateFrag = DateFragment()
    private val settingsFrag = SettingsFragment()
    private lateinit var firebaseAnalytics: FirebaseAnalytics





    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ResourceAsColor", "ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {


        var myToolbar = findViewById<Toolbar>(R.id.action_bar)



        myToolbar.showOverflowMenu()



        //Android less than O doesn't support dark navbar icons, so just make the navbar dark
        if ((Build.VERSION.SDK_INT <= Build.VERSION_CODES.O)) {
            window.navigationBarColor = ContextCompat.getColor(this, R.color.black)
        }



        super.onCreate(savedInstanceState)


        firebaseAnalytics = FirebaseAnalytics.getInstance(this)


        replaceFragment(dateFrag, R.id.constraintLayout)

        setContentView(R.layout.activity_main)

    }

    fun AppCompatActivity.replaceFragment(fragment: Fragment, frameId: Int) {
        supportFragmentManager.inTransaction{replace(frameId, fragment)}
    }

    inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> FragmentTransaction) {
        beginTransaction().func().commit()
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.overflow, menu)

        if(menu is MenuBuilder){
            //noinspection RestrictedApi
            menu.setOptionalIconsVisible(true)
        }

        return true
    }
}

