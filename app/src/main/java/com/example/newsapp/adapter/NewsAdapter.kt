package com.example.newsapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newsapp.R
import com.example.newsapp.databinding.ItemNewsBinding
import com.example.newsapp.model.Article

class NewsAdapter : RecyclerView.Adapter<NewsAdapter.ArticleViewHolder>() {
//    inner class ArticleViewHolder(itemView : View): RecyclerView.ViewHolder(itemView)
//    lateinit var articleImage : ImageView
//    lateinit var articleSource : TextView
//    lateinit var articleTitle : TextView
//    lateinit var articleDescription : TextView
//    lateinit var articleDateTime : TextView

    // ViewHolder using Data Binding
    class ArticleViewHolder(val itemBinding: ItemNewsBinding) : RecyclerView.ViewHolder(itemBinding.root)


    private val differCallBack = object : DiffUtil.ItemCallback<Article>(){
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem
        }

    }
    val differ = AsyncListDiffer(this,differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        return ArticleViewHolder(
            ItemNewsBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private var onItemClickListener : ((Article) -> Unit)? = null

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = differ.currentList[position]

//        articleImage = holder.itemView.findViewById(R.id.articleImage)
//        articleSource = holder.itemView.findViewById(R.id.articleSource)
//        articleTitle = holder.itemView.findViewById(R.id.articleTitle)
//        articleDescription = holder.itemView.findViewById(R.id.articleDescription)
//        articleDateTime = holder.itemView.findViewById(R.id.articleDateTime)

        holder.itemBinding.apply {
            Glide.with(root.context).load(article.urlToImage).into(articleImage)
            articleSource.text = article.source?.name
            articleTitle.text = article.title
            articleDescription.text = article.description
            articleDateTime.text = article.publishedAt

            root.setOnClickListener {
                onItemClickListener?.let {
                    it(article)
                }
            }
        }
    }
    fun setOnItemClickListener(listener : (Article) -> Unit){
        onItemClickListener = listener
    }
}