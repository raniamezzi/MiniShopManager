package com.example.minishopmanager

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

data class AiMessage(
    val text: String,
    val isUser: Boolean,
    val recommendedProducts: List<Product> = emptyList()
)

class AiAssistantBottomSheet : BottomSheetDialogFragment() {

    private lateinit var rvChat: RecyclerView
    private lateinit var etPrompt: EditText
    private lateinit var chatAdapter: AiChatAdapter
    private lateinit var dbHelper: DBHelper
    private var products: List<Product> = emptyList()
    private val messages = mutableListOf<AiMessage>()

    private val quickPrompts = listOf(
        "Cheapest item",
        "Compare phones",
        "Best under 1000 DT",
        "Show me laptops",
        "Best battery life",
        "Most expensive"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val ctx = requireContext()
        val density = resources.displayMetrics.density
        fun dp(value: Int) = (value * density).toInt()

        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.bg_card)
            setPadding(0, dp(12), 0, dp(16))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // Drag handle
        val handle = View(ctx).apply {
            setBackgroundColor(ContextCompat.getColor(ctx, R.color.border_subtle))
            layoutParams = LinearLayout.LayoutParams(dp(40), dp(4)).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                bottomMargin = dp(12)
            }
        }
        root.addView(handle)

        // Header
        val header = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(20), 0, dp(20), dp(12))
        }

        val icon = ImageView(ctx).apply {
            setImageResource(R.drawable.ic_sparkle)
            setColorFilter(ContextCompat.getColor(ctx, R.color.brand_blue))
            layoutParams = LinearLayout.LayoutParams(dp(22), dp(22))
        }
        header.addView(icon)

        val titleCol = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = dp(10)
            }
        }
        titleCol.addView(TextView(ctx).apply {
            text = "Mini Shop AI Assistant"
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(ctx, R.color.text_primary))
        })
        titleCol.addView(TextView(ctx).apply {
            text = "Ask about budgets, specs, or comparisons"
            textSize = 12f
            setTextColor(ContextCompat.getColor(ctx, R.color.text_secondary))
        })
        header.addView(titleCol)

        val btnClose = ImageButton(ctx).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setBackgroundResource(R.drawable.bg_circle_white)
            setColorFilter(ContextCompat.getColor(ctx, R.color.text_secondary))
            setPadding(dp(6), dp(6), dp(6), dp(6))
            layoutParams = LinearLayout.LayoutParams(dp(34), dp(34))
            setOnClickListener { dismiss() }
        }
        header.addView(btnClose)
        root.addView(header)

        // Divider
        root.addView(View(ctx).apply {
            setBackgroundColor(ContextCompat.getColor(ctx, R.color.border_subtle))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1))
        })

        // Chat RecyclerView
        rvChat = RecyclerView(ctx).apply {
            clipToPadding = false
            setPadding(dp(16), dp(12), dp(16), dp(12))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(310)
            )
        }
        root.addView(rvChat)

        // Quick-Tap Suggestion Chips Row
        val scrollSuggestions = HorizontalScrollView(ctx).apply {
            isHorizontalScrollBarEnabled = false
            clipToPadding = false
            setPadding(dp(16), 0, dp(16), dp(10))
        }
        val chipRow = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        renderSuggestionChips(chipRow)
        scrollSuggestions.addView(chipRow)
        root.addView(scrollSuggestions)

        // Input Row
        val inputRow = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), 0, dp(16), 0)
        }

        etPrompt = EditText(ctx).apply {
            hint = "Ask something..."
            textSize = 14f
            maxLines = 1
            imeOptions = EditorInfo.IME_ACTION_SEND
            inputType = InputType.TYPE_CLASS_TEXT
            setBackgroundResource(R.drawable.bg_input_field)
            setTextColor(ContextCompat.getColor(ctx, R.color.text_primary))
            setHintTextColor(ContextCompat.getColor(ctx, R.color.text_muted))
            setPadding(dp(16), 0, dp(16), 0)
            layoutParams = LinearLayout.LayoutParams(0, dp(46), 1f)
        }
        inputRow.addView(etPrompt)

        val btnSend = ImageButton(ctx).apply {
            setImageResource(android.R.drawable.ic_menu_send)
            setBackgroundResource(R.drawable.bg_circle_blue)
            setColorFilter(ContextCompat.getColor(ctx, R.color.white))
            setPadding(dp(10), dp(10), dp(10), dp(10))
            layoutParams = LinearLayout.LayoutParams(dp(46), dp(46)).apply {
                marginStart = dp(10)
            }
            setOnClickListener { submitPrompt(etPrompt.text.toString()) }
        }
        inputRow.addView(btnSend)
        root.addView(inputRow)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ctx = requireContext()
        dbHelper = DBHelper(ctx)
        products = JsonHelper.loadProducts(ctx)

        chatAdapter = AiChatAdapter(
            items = messages,
            onProductClick = { product ->
                val intent = Intent(ctx, ProductDetailActivity::class.java).apply {
                    putExtra("PRODUCT_ID", product.id)
                }
                startActivity(intent)
            },
            onAddToCartClick = { product ->
                dbHelper.addToCart(product.id, 1)
                (activity as? MainActivity)?.updateCartBadge()
                Toast.makeText(ctx, "${product.name} added to cart!", Toast.LENGTH_SHORT).show()
            }
        )

        rvChat.layoutManager = LinearLayoutManager(ctx).apply {
            stackFromEnd = true
        }
        rvChat.adapter = chatAdapter

        if (messages.isEmpty()) {
            addBotMessage(
                "Hi! I'm your Mini Shop AI assistant. Tap a quick suggestion below or ask me about prices, categories, or comparisons."
            )
        }

        etPrompt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                submitPrompt(etPrompt.text.toString())
                true
            } else {
                false
            }
        }
    }

    private fun renderSuggestionChips(container: LinearLayout) {
        container.removeAllViews()
        val density = resources.displayMetrics.density

        quickPrompts.forEach { promptText ->
            val chip = TextView(requireContext()).apply {
                text = promptText
                textSize = 12.5f
                setBackgroundResource(R.drawable.bg_pill_tint)
                setTextColor(ContextCompat.getColor(context, R.color.brand_blue_dark))
                setPadding(
                    (14 * density).toInt(),
                    (7 * density).toInt(),
                    (14 * density).toInt(),
                    (7 * density).toInt()
                )
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = (8 * density).toInt()
                }
                setOnClickListener {
                    submitPrompt(promptText)
                }
            }
            container.addView(chip)
        }
    }

    private fun submitPrompt(rawInput: String) {
        val clean = rawInput.trim()
        if (clean.isEmpty()) return

        messages.add(AiMessage(text = clean, isUser = true))
        chatAdapter.notifyItemInserted(messages.size - 1)
        etPrompt.text.clear()

        val (replyText, matchedProducts) = generateReply(clean)
        addBotMessage(replyText, matchedProducts)
    }

    private fun addBotMessage(text: String, recommended: List<Product> = emptyList()) {
        messages.add(AiMessage(text = text, isUser = false, recommendedProducts = recommended))
        chatAdapter.notifyItemInserted(messages.size - 1)
        rvChat.scrollToPosition(messages.size - 1)
    }

    private fun generateReply(query: String): Pair<String, List<Product>> {
        val q = query.lowercase().trim()
        val inStock = products.filter { it.stock > 0 }

        // 1. Cheapest / lowest price
        if (q.contains("cheap") || q.contains("lowest") || q.contains("affordable") || q.contains("least expensive") || q.contains("min price")) {
            val cheapest = inStock.sortedBy { it.price }.take(3)
            val text = "Here are our most affordable items in stock (tap + Cart to add directly):"
            return text to cheapest
        }

        // 2. Most expensive / premium
        if (q.contains("expensive") || q.contains("premium") || q.contains("highest") || q.contains("priciest")) {
            val priciest = inStock.sortedByDescending { it.price }.take(3)
            val text = "Here are our flagship high-end picks:"
            return text to priciest
        }

        // 3. Budget filter (e.g., "under 1000 DT")
        val budgetMatch = Regex("""(?:under|below|less than|max|<)\s*(\d+)""").find(q)
        if (budgetMatch != null) {
            val maxPrice = budgetMatch.groupValues[1].toDoubleOrNull() ?: 1000.0
            val matches = inStock.filter { it.price <= maxPrice }.sortedBy { it.price }
            return if (matches.isEmpty()) {
                val minStart = inStock.minOfOrNull { it.price }?.toInt() ?: 219
                "No items in stock under ${maxPrice.toInt()} DT. Our lowest-priced item starts at $minStart DT." to emptyList()
            } else {
                "Found ${matches.size} item(s) under ${maxPrice.toInt()} DT:" to matches.take(3)
            }
        }

        // 4. Category mapping with synonyms
        val matchedCategory = when {
            q.contains("phone") || q.contains("smartphone") || q.contains("mobile") -> "Smartphones"
            q.contains("laptop") || q.contains("macbook") || q.contains("pc") || q.contains("computer") -> "Laptops"
            q.contains("audio") || q.contains("headphone") || q.contains("earbud") || q.contains("airpod") || q.contains("sony") || q.contains("bose") -> "Audio"
            q.contains("wearable") || q.contains("watch") -> "Wearables"
            q.contains("accessor") || q.contains("charger") || q.contains("mouse") || q.contains("ssd") -> "Accessories"
            else -> null
        }

        // 5. Compare products
        if (q.contains("compare") || q.contains("vs")) {
            val pool = if (matchedCategory != null) {
                inStock.filter { it.category.equals(matchedCategory, ignoreCase = true) }
            } else {
                inStock
            }.take(3)

            return if (pool.isEmpty()) {
                "No products available to compare right now." to emptyList()
            } else {
                val text = "Side-by-side comparison:\n\n" +
                        pool.joinToString("\n\n") { item ->
                            "• ${item.name} — ${item.price.toInt()} DT\n  Battery: ${item.battery} | In stock: ${item.stock}"
                        }
                text to pool
            }
        }

        // 6. Category listing
        if (matchedCategory != null) {
            val catItems = inStock.filter { it.category.equals(matchedCategory, ignoreCase = true) }.take(3)
            return "Top $matchedCategory in stock:" to catItems
        }

        // 7. Battery life
        if (q.contains("battery") || q.contains("long lasting")) {
            val batteryPicks = inStock.filter { it.battery != "N/A" && it.battery.isNotEmpty() }.take(3)
            val text = "Great picks for battery life:\n" +
                    batteryPicks.joinToString("\n") { "• ${it.name} (${it.battery})" }
            return text to batteryPicks
        }

        val lowest = inStock.minByOrNull { it.price }
        val fallbackText = "I can help you find or compare products by budget, category, or specs! " +
                (if (lowest != null) "For example, our cheapest item is ${lowest.name} at ${lowest.price.toInt()} DT.\n\n" else "\n\n") +
                "Try tapping one of the quick suggestions below!"
        return fallbackText to (if (lowest != null) listOf(lowest) else emptyList())
    }

    private class AiChatAdapter(
        private val items: List<AiMessage>,
        private val onProductClick: (Product) -> Unit,
        private val onAddToCartClick: (Product) -> Unit
    ) : RecyclerView.Adapter<AiChatAdapter.MsgViewHolder>() {

        class MsgViewHolder(
            val container: LinearLayout,
            val tvBubble: TextView,
            val cardsContainer: LinearLayout
        ) : RecyclerView.ViewHolder(container)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MsgViewHolder {
            val ctx = parent.context
            val density = ctx.resources.displayMetrics.density
            fun dp(v: Int) = (v * density).toInt()

            val row = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, dp(4), 0, dp(6))
                layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            val tv = TextView(ctx).apply {
                textSize = 14f
                setPadding(dp(14), dp(10), dp(14), dp(10))
                maxWidth = (ctx.resources.displayMetrics.widthPixels * 0.80).toInt()
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            row.addView(tv)

            val cardsCol = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    (ctx.resources.displayMetrics.widthPixels * 0.82).toInt(),
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(6)
                }
            }
            row.addView(cardsCol)

            return MsgViewHolder(row, tv, cardsCol)
        }

        override fun onBindViewHolder(holder: MsgViewHolder, position: Int) {
            val msg = items[position]
            val ctx = holder.tvBubble.context
            val density = ctx.resources.displayMetrics.density
            fun dp(v: Int) = (v * density).toInt()
            val params = holder.tvBubble.layoutParams as LinearLayout.LayoutParams

            if (msg.isUser) {
                params.gravity = Gravity.END
                holder.tvBubble.setBackgroundResource(R.drawable.bg_pill_blue)
                holder.tvBubble.setTextColor(ContextCompat.getColor(ctx, R.color.white))
            } else {
                params.gravity = Gravity.START
                holder.tvBubble.setBackgroundResource(R.drawable.bg_pill_tint)
                holder.tvBubble.setTextColor(ContextCompat.getColor(ctx, R.color.text_primary))
            }
            holder.tvBubble.layoutParams = params
            holder.tvBubble.text = msg.text

            // Render interactive product action cards under bot messages
            holder.cardsContainer.removeAllViews()
            if (!msg.isUser && msg.recommendedProducts.isNotEmpty()) {
                holder.cardsContainer.visibility = View.VISIBLE
                msg.recommendedProducts.forEach { product ->
                    val card = LinearLayout(ctx).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER_VERTICAL
                        setBackgroundResource(R.drawable.bg_input_field)
                        setPadding(dp(12), dp(9), dp(10), dp(9))
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            bottomMargin = dp(6)
                        }
                        setOnClickListener { onProductClick(product) }
                    }

                    val infoCol = LinearLayout(ctx).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1f
                        ).apply {
                            marginEnd = dp(8)
                        }
                    }

                    infoCol.addView(TextView(ctx).apply {
                        text = product.name
                        textSize = 13f
                        setTypeface(null, Typeface.BOLD)
                        setTextColor(ContextCompat.getColor(ctx, R.color.text_primary))
                    })

                    infoCol.addView(TextView(ctx).apply {
                        text = "${ProductAdapter.formatPriceDT(product.price)} • ${product.category}"
                        textSize = 11.5f
                        setTextColor(ContextCompat.getColor(ctx, R.color.brand_blue_dark))
                    })

                    card.addView(infoCol)

                    val btnQuickAdd = TextView(ctx).apply {
                        text = "+ Cart"
                        textSize = 12f
                        setTypeface(null, Typeface.BOLD)
                        setBackgroundResource(R.drawable.bg_pill_blue)
                        setTextColor(ContextCompat.getColor(ctx, R.color.white))
                        setPadding(dp(12), dp(6), dp(12), dp(6))
                        setOnClickListener { onAddToCartClick(product) }
                    }
                    card.addView(btnQuickAdd)

                    holder.cardsContainer.addView(card)
                }
            } else {
                holder.cardsContainer.visibility = View.GONE
            }
        }

        override fun getItemCount(): Int = items.size
    }
}