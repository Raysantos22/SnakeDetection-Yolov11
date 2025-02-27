//package SnakeDetection
//
//import android.graphics.Color
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import com.SnakeDetection.SnakeInfo
//import com.SnakeDetection.R
//
//class SnakeLibraryAdapter(
//    private val snakes: List<SnakeInfo>,
//    private val onSnakeClicked: (SnakeInfo) -> Unit
//) : RecyclerView.Adapter<SnakeLibraryAdapter.SnakeViewHolder>() {
//
//    class SnakeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val imageView: ImageView = view.findViewById(R.id.imageSnake)
//        val nameText: TextView = view.findViewById(R.id.textSnakeName)
//        val dangerIndicator: View = view.findViewById(R.id.viewDangerIndicator)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SnakeViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_snake, parent, false)
//        return SnakeViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: SnakeViewHolder, position: Int) {
//        val snake = snakes[position]
//
//        holder.nameText.text = snake.name
//
//        // Set danger level indicator color
//        when (snake.dangerLevel) {
//            "Highly Venomous - Fatal", "Highly Venomous" ->
//                holder.dangerIndicator.setBackgroundColor(Color.RED)
//            "Venomous - Medical emergency" ->
//                holder.dangerIndicator.setBackgroundColor(Color.rgb(255, 165, 0)) // Orange
//            "Non-venomous but potentially dangerous" ->
//                holder.dangerIndicator.setBackgroundColor(Color.YELLOW)
//            else ->
//                holder.dangerIndicator.setBackgroundColor(Color.GREEN)
//        }
//
//        // Load image if resource ID is valid
//        if (snake.imageResId != 0) {
//            holder.imageView.setImageResource(snake.imageResId)
//        } else {
//            // Set placeholder
//            holder.imageView.setImageResource(R.drawable.placeholder_snake)
//        }
//
//        holder.itemView.setOnClickListener {
//            onSnakeClicked(snake)
//        }
//    }
//
//    override fun getItemCount() = snakes.size
//}