package com.shop.tcd

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shop.tcd.databinding.RecyclerviewItemRowBinding
import com.shop.tcd.model.Group


class RecyclerAdapter(private val groups: ArrayList<Group>) :
    RecyclerView.Adapter<RecyclerAdapter.GroupHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            RecyclerAdapter.GroupHolder {
//         val itemBinding = RecyclerviewItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//         return PaymentHolder(itemBinding)

        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_item_row, parent, false)
        val inflatedView =
            RecyclerviewItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        //  LayoutInflater.from(context).inflate(R.layout.recyclerview_item_row),parent,false)
        return GroupHolder(inflatedView)
    }

    /*fun onCreateViewHolder1(parent: ViewGroup, viewType: Int): GroupHolder {
      val inflatedView=parent.inflate(R.layout.recyclerview_item_row,false)
       return GroupHolder(inflatedView)
   }*/
    override fun getItemCount(): Int = groups.size

    override fun onBindViewHolder(holder: RecyclerAdapter.GroupHolder, position: Int) {
        val itemGroup = groups[position]
        /* val code: TextView
         val name: TextView
         code = holder.itemView.rootView.findViewById<TextView>(R.id.itemCode)
         name = holder.itemView.rootView.findViewById<TextView>(R.id.itemName)
         code.text = itemGroup.code
         name.text = itemGroup.name*/
        holder.bindGroup(itemGroup)
    }

    //1
    class GroupHolder(private val view: RecyclerviewItemRowBinding) :
        RecyclerView.ViewHolder(view.root),
        View.OnClickListener {
        //2
        private var group: Group? = null

        //3
        init {
            view.root.setOnClickListener(this)
        }

        fun bindGroup(group: Group) {
            this.group = group
            view.itemCode.text = group.code
            view.itemName.text = group.name
            /*  val code = view.findViewById<TextView>(R.id.itemCode)
              code.text = group.code
              val name = view.findViewById<TextView>(R.id.itemName)
              name.text = group.name*/
//            view.itemCode.text=group.code
//            view.itemName.text=group.name
//            view.itemDate.text = photo.humanDate
//            view.itemDescription.text = photo.explanation
        }

        //4
        override fun onClick(v: View) {
            /* val context = itemView.context
             val showPhotoIntent = Intent(context, LoginActivity::class.java)
             showPhotoIntent.putExtra(PHOTO_KEY, group)
             context.startActivity(showPhotoIntent)*/
            Log.d("RecyclerView", "CLICK!")
        }

        companion object {
            //5
            private val PHOTO_KEY = "PHOTO"
        }
    }


}