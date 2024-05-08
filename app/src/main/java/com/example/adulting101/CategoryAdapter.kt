import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.adulting101.CarMaintenanceActivity
import com.example.adulting101.CookingActivity
import com.example.adulting101.PersonalFinanceActivity
import com.example.adulting101.SelfCareActivity

class CategoryAdapter(private val context: Context, private val categories: List<String>) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.category_item, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val categoryName = categories[position]
        holder.categoryName.text = categoryName

        holder.itemView.setOnClickListener {
            val intent = when (categoryName) {
                "Cooking" -> Intent(context, CookingActivity::class.java)
                "Car Maintenance" -> Intent(context, CarMaintenanceActivity::class.java)
                "Self Care" -> Intent(context, SelfCareActivity::class.java)
                "Personal Finance" -> Intent(context, PersonalFinanceActivity::class.java)
                else -> null
            }
            intent?.let { context.startActivity(it) }
        }
    }

    override fun getItemCount(): Int = categories.size

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var categoryName: TextView = itemView.findViewById(R.id.categoryName)
    }
}
