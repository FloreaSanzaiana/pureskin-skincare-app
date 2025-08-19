package com.example.myapplication2.ui

import android.graphics.Paint
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.myapplication2.R
import com.example.myapplication2.data.model.Product
import com.example.myapplication2.data.model.ProductRecommended
import com.example.myapplication2.util.ImageSetter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ProductResultedFragment(private val product: ProductRecommended) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_product_details_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val img=R.id.productImage

        val imageView: ImageView = view?.findViewById(img) ?: return
        val imageResId = ImageSetter.getImageResourceId(product.area, product.product_type, requireContext())
        imageView.setImageResource(imageResId)

        imageView.post {
            imageView.layoutParams.height = imageView.height
        }

        val productNameTextView = view.findViewById<TextView>(R.id.productName)
        val productTypeTextView = view.findViewById<TextView>(R.id.productType)
        val productAreaTextView = view.findViewById<TextView>(R.id.productArea)
        val productTimeTextView = view.findViewById<TextView>(R.id.productTime)
        val productSpfTextView = view.findViewById<TextView>(R.id.productSpf)
        val productUrlTextView = view.findViewById<TextView>(R.id.url)
        val productIngredientsTextView = view.findViewById<TextView>(R.id.ingredients)
        val productIngredientsIrritating = view.findViewById<TextView>(R.id.ingredients_irritating)

        productNameTextView.text = product.product_name
        productTypeTextView.text = "\nProduct Type: ${product.product_type}\n\n"
        productAreaTextView.text = "Best for your skin: ${product.area}\n\n"
        productTimeTextView.text = "Perfect for: ${product.time}\n\n"

        productSpfTextView.text = when {
            product.spf == -1 -> "Sun Protection: -\n\n"
            product.spf == 0 -> "Sun Protection: Present but unspecified\n\n"
            else -> "Sun Protection: SPF ${product.spf}\n\n"
        }

        productUrlTextView.apply {
            text = "Shop Now: ${product.product_url}\n\n"
            if (product.product_url.isNotEmpty() && Patterns.WEB_URL.matcher(product.product_url).matches()) {
                Linkify.addLinks(this, Linkify.WEB_URLS)
                setLinkTextColor(ContextCompat.getColor(context, R.color.blue_pastel_dark))
                movementMethod = LinkMovementMethod.getInstance()
                paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
            } else {
                text = "Invalid URL"
            }
        }

        val formattedIngredients = product.clean_ingreds
            .removeSurrounding("[", "]")
            .replace("\'", "")
            .replace(",", ", ")
            .replace("\\s+".toRegex(), " ")
            .trim()
        productIngredientsTextView.text = "Key Ingredients: \n\n$formattedIngredients"

        val formattedIngredientsIrritations = product.irritating_ingredients
            .removeSurrounding("[", "]")
            .replace("'", "")
            .replace(",", ", ")
            .replace("\\s+".toRegex(), " ")
            .trim()

        val displayText = if (formattedIngredientsIrritations.isEmpty()) {
            "-"
        } else {
            formattedIngredientsIrritations
        }

        productIngredientsIrritating.text = "\n\nIngredients that may cause irritation:\n\n$displayText"

    }


}

