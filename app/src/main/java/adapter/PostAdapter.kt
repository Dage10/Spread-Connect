package adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.daviddam.clickconnect.databinding.ItemPostBinding
import com.daviddam.clickconnect.R
import models.Post
import util.ImageExtension.loadImageOrDefault


class PostAdapter(
    private var posts: List<Post>,
    private val idUsuariLoguejat: String?,
    private val onEditar: (Post) -> Unit,
    private val onEliminar: (Post) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    fun updateData(nous: List<Post>) {
        posts = nous
        notifyDataSetChanged()
    }

    inner class PostViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPostBinding.inflate(inflater, parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, posicio: Int) {
        val post = posts[posicio]
        val esDelMateixUsuari = idUsuariLoguejat == post.id_usuari

        holder.binding.apply {
            tvUsuari.text = post.nom_usuari ?: "Usuari"
            tvTitol.text = post.titol
            tvDescripcio.text = post.descripcio
            
            imgPost.loadImageOrDefault(post.imatge_url, R.drawable.avatar)
            imgPost.visibility = if (post.imatge_url.isNullOrEmpty()) View.GONE else View.VISIBLE

            btnEditar.visibility = if (esDelMateixUsuari) View.VISIBLE else View.GONE
            btnEliminar.visibility = if (esDelMateixUsuari) View.VISIBLE else View.GONE

            btnEditar.setOnClickListener { onEditar(post) }
            btnEliminar.setOnClickListener { onEliminar(post) }
        }
    }

    override fun getItemCount(): Int = posts.size
}
