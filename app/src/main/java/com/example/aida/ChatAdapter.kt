package com.example.aida

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aida.DataBase.Message


class ChatAdapter(private var dataSet: List<Message>) :
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView
        val imageView: ImageView

        init {
            // Define click listener for the ViewHolder's View
            textView = view.findViewById(R.id.textView)
            imageView = itemView.findViewById(R.id.userImageView)

        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item

        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.text_row_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element

        viewHolder.textView.text = dataSet[position].content

        val layoutParams = viewHolder.imageView.layoutParams as RelativeLayout.LayoutParams
        val layoutParamsText = viewHolder.textView.layoutParams as RelativeLayout.LayoutParams
        if (dataSet[position].isUser){
            //Condiciones del texto
            viewHolder.textView.setBackgroundResource(R.drawable.rounded_circle_right_arrow)
            layoutParamsText.removeRule(RelativeLayout.END_OF)  //Eliminar posicion end del texto al user image
            layoutParamsText.addRule(RelativeLayout.START_OF, R.id.userImageView) //Añadir que texto este al start de userimage
            viewHolder.textView.layoutParams = layoutParamsText   //Update conditions

            //Condiciones del imageView
            layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_START) //Remove rule parent start
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END)      //Add rule parent end
            viewHolder.imageView.layoutParams = layoutParams           //Update conditions

        }else{
            viewHolder.textView.setBackgroundResource(R.drawable.rounded_circle_left_arrow)
            layoutParamsText.removeRule(RelativeLayout.START_OF)
            layoutParamsText.addRule(RelativeLayout.END_OF, R.id.userImageView)
            viewHolder.textView.layoutParams = layoutParams

            //image pos
            layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_END)
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START)
            viewHolder.imageView.layoutParams = layoutParams
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    //Agregada funcion para actualizar la lista de message
    fun updateData(newData: List<Message>) {
        dataSet = newData
    }
}