package com.example.aida

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.aida.DataBase.Database
import com.example.aida.DataBase.Message
import com.example.aida.DataBase.MessageDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [chat.newInstance] factory method to
 * create an instance of this fragment.
 */
class chat : Fragment() {

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var messagechat: List<Message> = listOf()
    private var adapter: ChatAdapter = ChatAdapter(messagechat)
    private lateinit var recyclerView: RecyclerView
    private lateinit var db:Database
    private lateinit var messageDao: MessageDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //context?.deleteDatabase("message_list") para borrar database
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)

        }
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                db = Room.databaseBuilder(requireContext(), Database::class.java, "message_list")
                    .build()
                messageDao = db.messageDao()
                val cal = Calendar.getInstance()
                cal.time = Date()
                cal.add(Calendar.DAY_OF_MONTH,-7)
                messageDao.archiveAll(cal.time)
                messagechat = messageDao.getAll()
                adapter.updateData(messagechat)
                adapter.notifyDataSetChanged()
            }

            withContext(Dispatchers.Main) {
                //recyclerView.smoothScrollToPosition(messagechat.size - 1)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_chat, container, false)
        if (! Python.isStarted()) {
            Python.start(AndroidPlatform(requireContext()));
        }

        return rootView
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment chat.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            chat().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ElBoton = view.findViewById<Button>(R.id.btn_send)

        ElBoton.setOnClickListener {btn_sender()}
        // Inflate the layout for this fragment

        recyclerView = view.findViewById<RecyclerView>(R.id.rv_messages)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

    }

    fun btn_sender(){
        val text = view?.findViewById<EditText>(R.id.et_message)
        if (text != null && text.toString() != "") {
            //messagechat.add(MessageProps(text.text.toString(), true))


            lifecycleScope.launch {
                withContext(Dispatchers.IO){
                    messageDao.insert(Message(0, text.text.toString(), true, Date().time.toLong(), false))
                    val msg = messageDao.getAll()
                    messagechat = msg
                }
                withContext(Dispatchers.Main) {
                    adapter.updateData(messagechat)
                    adapter.notifyDataSetChanged()
                    recyclerView.smoothScrollToPosition(messagechat.size - 1)
                }

            }



        }
        val itemCount = adapter.getItemCount()

        print("size con el metodo getItemCount del adapter: $itemCount")
        println("El size de message fuera del proceso lyfecyclescope: " + messagechat.size)

        text?.setText("")


    }



}




