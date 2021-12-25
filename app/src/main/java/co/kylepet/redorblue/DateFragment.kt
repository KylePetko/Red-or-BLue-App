package co.kylepet.redorblue

import android.Manifest
import android.accounts.Account
import android.accounts.AccountManager
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.content.ContentValues.TAG
import android.content.Context.MODE_PRIVATE
import android.os.Bundle

import android.util.Log
import android.widget.TextView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import java.util.*
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.opengl.Visibility
import android.os.Build
import android.os.Handler
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Patterns
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.common.reflect.Reflection.getPackageName
import org.w3c.dom.Text
import java.lang.Exception
import kotlin.concurrent.fixedRateTimer
import kotlin.concurrent.schedule
import kotlin.concurrent.scheduleAtFixedRate


class DateFragment : Fragment() {

    private val APPLICATION_NAME = "Red or Blue"
    private val JSON_FACTORY = JacksonFactory.getDefaultInstance()
    private val TOKENS_DIRECTORY_PATH = "data/data/co.kylepet.redorblue/"

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private val SCOPES = listOf(CalendarScopes.CALENDAR_READONLY)
    //private val CREDENTIALS_FILE_PATH: InputStream? = context?.assets?.open("credentials.json")


    private val CREDENTIALS_FILE_PATH = "data/data/co.kylepet.redorblue/assets/credentials.json"
    var email = ""

    lateinit var rorbText: TextView
    lateinit var pullToRefresh: SwipeRefreshLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {


        setHasOptionsMenu(true)

        var view =  inflater.inflate(R.layout.rorb_fragment, container,false)

        rorbText = view.findViewById(R.id.rorb_text) as TextView

        rorbText.visibility = View.GONE

        pullToRefresh = view.findViewById(co.kylepet.redorblue.R.id.pullToRefresh) as SwipeRefreshLayout



        pullToRefresh.setOnRefreshListener {
            readCal(rorbText, pullToRefresh, requireContext(), true, true)


        }

        readCal(rorbText, pullToRefresh,  requireContext(), true, false)

        return view
    }

    var firstRun = true

    fun readCal(rorbText: TextView, pullRefresh: SwipeRefreshLayout, context: Context, changeColor: Boolean, fromRefresh: Boolean){




        // Here, thisActivity is the current activity
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {



            // Permission is not granted
            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.READ_CONTACTS)) {

                var alertBuilder = AlertDialog.Builder(context)
       alertBuilder.setCancelable(true)
       alertBuilder.setTitle("Permission denied")
       alertBuilder.setMessage("The calendar permission is required in order to read the school's calendar and determine if tomorrow is red or blue or if there is no school.")
       alertBuilder.setOnDismissListener {

           if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
           requestPermissions(
               arrayOf(Manifest.permission.READ_CONTACTS),
               2)

       }
                alertBuilder.setPositiveButton("Ah, okay") { _: DialogInterface, _: Int ->


           //requestPermissions(
            //    arrayOf(Manifest.permission.READ_CONTACTS),
             //   2)

       }


                var alert = alertBuilder.create()
                pullRefresh.isRefreshing = false
                alert.show()




            } else {



                // No explanation needed, we can request the permission.
                requestPermissions(
                    arrayOf(Manifest.permission.READ_CONTACTS),
                    2)






                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {



            // Permission has already been granted

            var  HTTP_TRANSPORT = com.google.api.client.http.javanet.NetHttpTransport()

            var  editor = context.getSharedPreferences("prefs", MODE_PRIVATE).edit()


            var  emailPattern = Patterns.EMAIL_ADDRESS // API level 8+
            var  accounts = AccountManager.get(context).accounts
            for (account: Account in accounts) {
                if (emailPattern.matcher(account.name).matches()) {

                    email = account.name
                }
            }


            val thread = Thread(Runnable {
                // var  service =  com.google.api.services.tasks.Tasks.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                //    .setApplicationName(APPLICATION_NAME)
                //  .build()

                var credential = GoogleAccountCredential.usingOAuth2(context, Arrays.asList(
                    CalendarScopes.CALENDAR_READONLY
                ))

                if(email != ""){
                    credential.selectedAccountName = email



                    var mService = Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                        .setApplicationName(APPLICATION_NAME)
                        .build()

                    //Check for internet


                    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo


                    if(true) {

                        val calendar = java.util.Calendar.getInstance()

                        calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
                        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                        calendar.set(java.util.Calendar.MINUTE, 0)
                        calendar.set(java.util.Calendar.SECOND, 0)
                        calendar.set(java.util.Calendar.MILLISECOND, 0)

                        val today = DateTime(calendar.time)

                        calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)

                        val tomorrow = DateTime(calendar.time)


                        //Only run once, or if the user pulls down to refresh, and connected to internet
                        if((firstRun || fromRefresh) && activeNetwork?.isConnected == true) {
                            try {

                                firstRun = false

                                activity?.runOnUiThread() {
                                    pullRefresh.isRefreshing = true

                                }


                                //val now = DateTime(calendar.timeInMillis)

                                Log.d("Time", today.toString())



                                try {
                                    if (activeNetwork.isConnected) {
                                        val events = mService.events()
                                            .list("lgsuhsd.org_tebqf0pqvog3p4s5flsmddg4u8@group.calendar.google.com")
                                            .setMaxResults(10)
                                            .setTimeMin(today)
                                            .setTimeMax(tomorrow)
                                            .setOrderBy("startTime")
                                            .setSingleEvents(true)
                                            .execute()

                                        //Keeps track if red or blue is found
                                        var found = false

                                        var items = events.items
                                        if (items.isEmpty()) {
                                            System.out.println("No upcoming events found.")

                                            if (items.isEmpty()) {
                                                System.out.println("No upcoming events found.")

                                                activity?.runOnUiThread {


                                                    rorbText.text = "Tomorrow there is no school"
                                                    rorbText.visibility = View.VISIBLE

                                                    editor.putString("date", today.toString())
                                                    editor.apply()
                                                    editor.putString("dayType", "Tomorrow there is no school")
                                                    editor.apply()

                                                    if (changeColor)
                                                        animateText(ContextCompat.getColor(context, R.color.white))
                                                }
                                            }


                                        } else {
                                            System.out.println("Upcoming events")



                                            for (event in items) {
                                                var start = event.start.dateTime
                                                if (start == null) {
                                                    start = event.start.date
                                                }
                                                System.out.printf("%s (%s)\n", event.summary, start)

                                                if (items.isEmpty()) {
                                                    System.out.println("No upcoming events found.")
                                                }



                                                if (event.summary == "RED") {
                                                    activity?.runOnUiThread {


                                                        rorbText.text = "Tomorrow is a red day"
                                                        rorbText.visibility = View.VISIBLE
                                                        found = true

                                                        if (changeColor)
                                                            animateText(ContextCompat.getColor(context, R.color.tRed))

                                                        editor.putString("date", today.toString())
                                                        editor.apply()
                                                        editor.putString("dayType", "Tomorrow is a red day")
                                                        editor.apply()

                                                    }

                                                    break
                                                } else if (event.summary == "BLUE") {
                                                    activity?.runOnUiThread {


                                                        rorbText.text = "Tomorrow is a blue day"
                                                        rorbText.visibility = View.VISIBLE
                                                        found = true

                                                        if (changeColor)
                                                            animateText(ContextCompat.getColor(context, R.color.tBlue))

                                                        editor.putString("date", today.toString())
                                                        editor.apply()
                                                        editor.putString("dayType", "Tomorrow is a blue day")


                                                        editor.apply()
                                                    }

                                                    break
                                                }

                                            }
                                        }
                                        activity?.runOnUiThread {


                                            if (!found) {
                                                rorbText.text = "Tomorrow there is no school"
                                                rorbText.visibility = View.VISIBLE

                                                if (changeColor)
                                                    animateText(ContextCompat.getColor(context, R.color.white))

                                                editor.putString("date", today.toString())
                                                editor.apply()
                                                editor.putString("dayType", "Tomorrow there is no school")
                                                editor.apply()

                                            }
                                        }
                                    } else {
                                        activity?.runOnUiThread {
                                            startActivity(Intent(context, ErrorNoInternet::class.java))

                                        }
                                    }
                                }
                                catch (e: java.net.UnknownHostException){
                                    activity?.runOnUiThread {
                                        startActivity(Intent(context, ErrorNoInternet::class.java))

                                    }
                                    e.printStackTrace()
                                }
                                catch (e: javax.net.ssl.SSLException){
                                    activity?.runOnUiThread {
                                        startActivity(Intent(context, ErrorNoInternet::class.java))

                                    }
                                    e.printStackTrace()

                                }

                                catch (e: java.net.ConnectException ){
                                    activity?.runOnUiThread {
                                        startActivity(Intent(context, ErrorNoInternet::class.java))

                                    }
                                    e.printStackTrace()

                                }

                                catch (e:  java.lang.IllegalArgumentException){
                                    activity?.runOnUiThread {
                                        startActivity(Intent(context, ErrorNoInternet::class.java))

                                    }
                                    e.printStackTrace()

                                }





                            } catch (e: UserRecoverableAuthIOException) {

                                startActivityForResult(e.intent, 11)
                            }

                        }
                        else{



                            // Return value from preferences
                            var prefs = context.getSharedPreferences("prefs", MODE_PRIVATE)








                            if(prefs.getString("date", "") == today.toString()){
                                activity?.runOnUiThread{

                                    rorbText.text = prefs.getString("dayType", "Whoops, there is an error")
                                    rorbText.visibility = View.VISIBLE

                                    if(changeColor && prefs?.getString("dayType", "Whoops, there is an error")?.contains("no school")!!)
                                        animateText(ContextCompat.getColor(context, R.color.white))

                                    else if(changeColor && prefs?.getString("dayType", "Whoops, there is an error")?.contains("red")!!)
                                        animateText(ContextCompat.getColor(context, R.color.tRed))

                                    else if(changeColor && prefs?.getString("dayType", "Whoops, there is an error")?.contains("blue")!!)
                                        animateText(ContextCompat.getColor(context, R.color.tBlue))
                                }
                            }
                            else{ //No internet and no saved value

                                //if(activeNetwork?.isConnected == false)

                                activity?.runOnUiThread {
                                    startActivity(Intent(context, ErrorNoInternet::class.java))

                                }
                            }


                        }
                    }






                }

                else{
                    val intent = Intent(context, ErrorNoEmail::class.java)
                    startActivity(intent)

                }




                activity?.runOnUiThread {
                    pullRefresh.isRefreshing = false

                }




            })
            thread.start()


        }

    }




    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater ) {

        Log.d(TAG, "Fragment.onCreateOptionsMenu")


        return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.donateButton -> {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.me/kailepetko")))

                return true

            }

            R.id.menu_refresh -> {


                var rorbText = view?.findViewById(R.id.rorb_text) as TextView

                val pullToRefresh = view?.findViewById(co.kylepet.redorblue.R.id.pullToRefresh) as SwipeRefreshLayout

                pullToRefresh.isRefreshing = true

                pullToRefresh.setOnRefreshListener {
                    readCal(rorbText, pullToRefresh, requireContext(), true, true)


                }

                readCal(rorbText, pullToRefresh,  requireContext(), true, true)

            }

            R.id.menu_calendar -> {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://calendar.google.com/calendar/embed?src=lgsuhsd.org_tebqf0pqvog3p4s5flsmddg4u8%40group.calendar.google.com&ctz=America%2FLos_Angeles")))

                return true




            }

            R.id.menu_settings -> {
                var myIntent = Intent(context, NotificationActivity::class.java)
                startActivity(myIntent)

            }

            R.id.menu_feedback -> {

            var uri = Uri.parse("mailto:")
            .buildUpon()
            .appendQueryParameter("to", "feedback@kylepet.co")
            .appendQueryParameter("subject", "Red or Blue Feedback: ")
            .appendQueryParameter("body", "Feedback: ")
            .build()

            var emailIntent =  Intent(Intent.ACTION_SENDTO, uri)
            startActivity(Intent.createChooser(emailIntent, "Send feedback using:"))

                return true
            }

        }


        return super.onOptionsItemSelected(item)
    }

    fun animateText(cTo: Int){

       // val window = activity?.window

     //   var myToolbar = activity?.findViewById(R.id.action_bar) as Toolbar

        var rorbText = activity?.findViewById<TextView>(R.id.rorb_text)

        var colorFrom =  ContextCompat.getColor(context!!, R.color.colorPrimary)

        var colorTo = cTo

        val colorAnimation = ValueAnimator.ofObject( ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation.duration = 2000
        colorAnimation.addUpdateListener { animator ->
            val color = animator.animatedValue as Int

            rorbText?.setTextColor(color)

           //myToolbar.setBackgroundColor(color)
           //window?.statusBarColor = color
            //window?.navigationBarColor = color





        }
        colorAnimation.start()

    }

    internal fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent ) {
        super.onActivityResult(requestCode, resultCode, data);

        when(requestCode){


            //When it asks the user to log into google
            11 -> {


                Log.i(TAG, "onActivityResult called.");


                        readCal(view?.findViewById(R.id.rorb_text)!!, view?.findViewById(R.id.pullToRefresh)!!,  requireContext(), true, true)
                }


                //}

            }


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        if (requestCode == 2 && !(grantResults.isEmpty())) {
            Log.i(TAG, "Received response for contact permissions request.");

            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){

                activity?.runOnUiThread{
                    readCal(view?.findViewById(R.id.rorb_text)!!, view?.findViewById(R.id.pullToRefresh)!!,  requireContext(), true, false)

                }



            } else if(! shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)){
                //The user checked never show this again on the permission dialog

                var alertBuilder = AlertDialog.Builder(requireContext())
                alertBuilder.setCancelable(true)
                alertBuilder.setTitle("Permission denied")
                alertBuilder.setMessage("The calendar permission is required for this app to work. Go into this app's info, then go into permissions and enable the contacts permission." +
                        " You can also clear this app's data.")
                alertBuilder.setOnDismissListener {

                    var intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + "co.kylepet.redorblue"))
                    intent.addCategory(Intent.CATEGORY_DEFAULT)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                   // intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    intent.putExtra("Exit me", true)


                    startActivity(intent)

                    activity?.finish()

                }
                alertBuilder.setPositiveButton("Open app info") { _: DialogInterface, _: Int -> }




                var alert = alertBuilder.create()

                view?.findViewById<SwipeRefreshLayout>(R.id.pullToRefresh)!!.isRefreshing = false

                alert.show()




            }

                else if(grantResults[0] == PackageManager.PERMISSION_DENIED){
                activity?.runOnUiThread{
                    //readCal(view?.findViewById(R.id.rorb_text)!!, view?.findViewById(R.id.pullToRefresh)!!,  context!!, true, false)

                    requestPermissions(
                        arrayOf(Manifest.permission.READ_CONTACTS),
                        2)
                }

            }

        } else {

            view?.findViewById<SwipeRefreshLayout>(R.id.pullToRefresh)!!.isRefreshing = false


            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
}