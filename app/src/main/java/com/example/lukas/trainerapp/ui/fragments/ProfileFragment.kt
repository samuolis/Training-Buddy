package com.example.lukas.trainerapp.ui.fragments


import android.graphics.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.lukas.trainerapp.ui.NavigationActivity
import com.example.lukas.trainerapp.R
import com.example.lukas.trainerapp.db.entity.User
import com.example.lukas.trainerapp.db.viewmodel.UserViewModel
import kotlinx.android.synthetic.main.fragment_account_edit.*
import kotlinx.android.synthetic.main.fragment_profile.*


/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class ProfileFragment : Fragment() {

    lateinit var bitmap : Bitmap
    private val r = Rect()
    lateinit var userViewModel : UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var rootView = inflater.inflate(R.layout.fragment_profile, container, false)
        userViewModel = ViewModelProviders.of(activity!!).get(UserViewModel::class.java)
        rootView.post({
            setupInfo()
        })
        return rootView
    }

    private fun setupInfo(){
        setupInitials()
        userViewModel.user.observe(this, Observer { user: User ->
            user_full_name_text_view.text = user.fullName
            user_email_text_view.text = user.email
            user_phone_number_text_view.text = user.phoneNumber
        })
        user_full_name_card_holder.setOnClickListener({
            (activity as NavigationActivity).showDialog()
        })
        user_email_card_holder.setOnClickListener({
            (activity as NavigationActivity).showDialog()
        })
        user_phone_number_card_holder.setOnClickListener({
            (activity as NavigationActivity).showDialog()
        })

    }

    private fun setupInitials(){
        bitmap = Bitmap.createBitmap(initials_image_view.width, initials_image_view.height, Bitmap.Config.ARGB_8888)
        var canvas = Canvas(bitmap)
        var paint = Paint()
        userViewModel.user.observe(this, Observer {user: User? ->
            var firstAndLastName = user?.fullName?.split(" ")
            var initials = ""
            firstAndLastName?.forEach { name ->
                initials = initials + name.get(0)
            }
            setTextSizeForWidth(paint,initials_image_view.width.toFloat()-100, initials)
            drawCenter(canvas, paint, initials)
            initials_image_view!!.setImageBitmap(bitmap)
        })
    }

    private fun drawCenter(canvas: Canvas, paint: Paint, text: String) {
        canvas.getClipBounds(r)
        val cHeight = r.height()
        val cWidth = r.width()
        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.WHITE
        paint.getTextBounds(text, 0, text.length, r)
        val x = cWidth / 2f - r.width() / 2f - r.left
        val y = cHeight / 2f + r.height() / 2f - r.bottom
        canvas.drawText(text, x, y, paint)
    }

    private fun setTextSizeForWidth(paint: Paint, desiredWidth: Float,
                                    text: String) {

        // Pick a reasonably large value for the test. Larger values produce
        // more accurate results, but may cause problems with hardware
        // acceleration. But there are workarounds for that, too; refer to
        // http://stackoverflow.com/questions/6253528/font-size-too-large-to-fit-in-cache
        val testTextSize = 48f

        // Get the bounds of the text, using our testTextSize.
        paint.textSize = testTextSize
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)

        // Calculate the desired size as a proportion of our testTextSize.
        val desiredTextSize = testTextSize * desiredWidth / bounds.width()

        // Set the paint for that size.
        paint.textSize = desiredTextSize
    }

}
