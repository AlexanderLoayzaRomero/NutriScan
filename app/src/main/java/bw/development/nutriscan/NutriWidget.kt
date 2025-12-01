package bw.development.nutriscan

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import bw.development.nutriscan.R

class NutriWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
}

internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
    // 1. Leer los datos
    val prefs = context.getSharedPreferences("widget_data", Context.MODE_PRIVATE)
    val total = prefs.getInt("total_calories", 0)
    val goal = prefs.getInt("goal_calories", 2000)

    // Leer Macros
    val protein = prefs.getInt("widget_protein", 0)
    val fat = prefs.getInt("widget_fat", 0)
    val carbs = prefs.getInt("widget_carbs", 0)

    // 2. Calcular progreso (evitar división por cero)
    val progress = if (goal > 0) (total * 100) / goal else 0

    // 3. Actualizar la vista
    val views = RemoteViews(context.packageName, R.layout.widget_nutri)

    // Textos
    views.setTextViewText(R.id.widget_calories_text, "$total / $goal kcal")
    views.setTextViewText(R.id.widget_protein, "${protein}g Prot")
    views.setTextViewText(R.id.widget_fat, "${fat}g Gras")
    views.setTextViewText(R.id.widget_carbs, "${carbs}g Carb")

    // Barra de Progreso
    views.setProgressBar(R.id.widget_progress_bar, 100, progress, false)

    // 4. Notificar al sistema
    appWidgetManager.updateAppWidget(appWidgetId, views)
}