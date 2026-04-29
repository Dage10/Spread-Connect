package adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.daviddam.spreadconnect.databinding.ItemPostTagBinding

class PostTagAdapter(
    private var tags: List<String>
) : RecyclerView.Adapter<PostTagAdapter.TagViewHolder>() {

    fun updateData(nous: List<String>) {
        tags = nous
        notifyDataSetChanged()
    }

    inner class TagViewHolder(val binding: ItemPostTagBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val binding = ItemPostTagBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TagViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TagViewHolder, posicio: Int) {
        holder.binding.btnTag.text = tags[posicio]
    }

    override fun getItemCount(): Int = tags.size
}
