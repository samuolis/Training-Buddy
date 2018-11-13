package com.example.lukas.trainerapp.ui.fragments


import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

import androidx.fragment.app.DialogFragment
import com.example.lukas.trainerapp.R
import kotlinx.android.synthetic.main.activity_navigation.*


class AccountEditDialogFragment : DialogFragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        // Inflate the layout to use as dialog or embedded fragment
        return inflater.inflate(R.layout.fragment_account_edit, container, false)
    }

    /** The system calls this only when creating the layout in a dialog. */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        val dialog = super.onCreateDialog(savedInstanceState)
        return dialog
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }
}
