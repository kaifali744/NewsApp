package com.example.newsapp.ui.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.MainActivity
import com.example.newsapp.R
import com.example.newsapp.adapter.NewsAdapter
import com.example.newsapp.databinding.FragmentSearchBinding
import com.example.newsapp.ui.NewsViewModel
import com.example.newsapp.util.Constants
import com.example.newsapp.util.Constants.Companion.SEARCH_NEWS_TIME_DELAY
import com.example.newsapp.util.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchFragment : Fragment(R.layout.fragment_search) {

    private var _binding: FragmentSearchBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    lateinit var newsViewModel: NewsViewModel
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var retryButton: Button
    private lateinit var errorText: TextView
    private lateinit var itemSearchError: CardView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        itemSearchError = view.findViewById(R.id.itemSearchError)

        // Error view ko inflate karna
        val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.item_error, null)

        retryButton = view.findViewById(R.id.retryButton)
        errorText = view.findViewById(R.id.errorText)

        // ViewModel ko initialize karna jo MainActivity se liya gaya hai
        newsViewModel = (activity as MainActivity).newsViewModel
        setupSearchRecycler()

        // News item click hone par articleFragment mein navigate karna
        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article",it)
            }
            findNavController().navigate(R.id.action_navigation_search_to_articleFragment, bundle)
        }

        var job : Job? = null
        binding.searchEdit.addTextChangedListener { editable ->
            job?.cancel()  // Cancel the previous job to avoid multiple searches being triggered
            job = lifecycleScope.launch { // Using lifecycleScope for a lifecycle-aware coroutine
                delay(SEARCH_NEWS_TIME_DELAY) // Wait for the user to stop typing for the specified delay
                editable?.let {
                    if (it.toString().isNotEmpty()) {  // Perform the search only if the text is not empty
                        newsViewModel.searchNews(it.toString())  // Trigger the search
                    }
                }
            }
        }

        // Observing the headlines data from ViewModel
        newsViewModel.searchNews.observe(viewLifecycleOwner, Observer { response ->
            when(response){
                is Resource.Success<*> -> {
                    hideProgressBar()
                    hideErrorMessage()
                    response.data?.let { newsResponse ->
                        // News ko adapter mein submit karna
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPages = newsResponse.totalResults/ Constants.QUERY_PAGE_SIZE +2
                        isLastPage = newsViewModel.searchNewsPage == totalPages
                        if (isLastPage){
                            binding.recyclerSearch.setPadding(0,0,0,0)
                        }
                    }
                }

                is Resource.Error<*> -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        // Agar error aaye toh user ko toast show karna
                        Toast.makeText(activity, "Sorry error: $message", Toast.LENGTH_LONG).show()
                        showErrorMessage(message)
                    }
                }

                is Resource.Loading<*> -> {
                    showProgressBar()
                }
            }
        })

        // Retry button pe click hone par news fetch karna
        retryButton.setOnClickListener {
            if (binding.searchEdit.toString().isNotEmpty()){
                newsViewModel.searchNews(binding.searchEdit.toString())
            }else{
                hideErrorMessage()
            }
        }
    }

    var isError = false
    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    // Progress bar ko hide karna
    private fun hideProgressBar(){
        binding.paginationProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    // Progress bar ko show karna
    private fun showProgressBar(){
        binding.paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    // Error message ko hide karna
    private fun hideErrorMessage(){
        binding.itemSearchError.visibility = View.INVISIBLE
        isError = false
    }

    // Error message ko show karna
    private fun showErrorMessage(message : String){
        binding.itemSearchError.visibility = View.VISIBLE
        errorText.text = message
        isError = true
    }

    // Scroll listener ko define karna jo pagination ka kaam karega
    private val scrollListener = object : RecyclerView.OnScrollListener(){

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            // Pagination ka logic
            val isNoErrors = !isError
            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= Constants.QUERY_PAGE_SIZE
            val shouldPaginate = isNoErrors && isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning && isTotalMoreThanVisible && isScrolling

            // Agar conditions match ho, toh next page load karna
            if (shouldPaginate){
                newsViewModel.searchNews(binding.searchEdit.text.toString())
                isScrolling = false
            }
        }

        // Scroll state change hone par isScrolling ko true karna
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                isScrolling = true
            }
        }
    }

    // RecyclerView setup karna
    private fun setupSearchRecycler(){
        newsAdapter = NewsAdapter()
        binding.recyclerSearch.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@SearchFragment.scrollListener)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}