import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shop.tcd.R
import com.shop.tcd.model.Group

class BlogAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var items: List<Group> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return BlogViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.single_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is BlogViewHolder -> {
                holder.bind(items.get(position))
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun submitList(blogList: List<Group>) {
        items = blogList
    }

    class BlogViewHolder constructor(
        itenView: View,
    ) : RecyclerView.ViewHolder(itenView) {
        val code: TextView = itemView.findViewById(R.id.itemCode)
        fun bind(group: Group) {
            code.setText(group.code)
        }
    }
}