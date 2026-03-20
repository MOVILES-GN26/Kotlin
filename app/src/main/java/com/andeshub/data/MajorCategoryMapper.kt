package com.andeshub.data

fun getRecommendedCategories(major: String?): List<String> {
    return when (major) {
        "Ingeniería de Sistemas y Computación",
        "Ingeniería Eléctrica",
        "Ingeniería Electrónica" -> listOf("Electronics", "Books & Supplies", "Tutoring & Services")

        "Ingeniería Civil",
        "Ingeniería Mecánica",
        "Ingeniería Química",
        "Ingeniería Industrial",
        "Ingeniería Biomédica" -> listOf("Books & Supplies", "Tutoring & Services", "Electronics")

        "Medicina",
        "Biología",
        "Química",
        "Física",
        "Matemáticas",
        "Geología" -> listOf("Books & Supplies", "Tutoring & Services", "Other")

        "Administración de Empresas",
        "Economía",
        "Ciencia Política" -> listOf("Books & Supplies", "Electronics", "Tutoring & Services")

        "Derecho",
        "Filosofía",
        "Historia",
        "Literatura",
        "Sociología",
        "Antropología",
        "Geografía",
        "Psicología" -> listOf("Books & Supplies", "Tutoring & Services", "Other")

        "Arquitectura",
        "Diseño",
        "Arte" -> listOf("Books & Supplies", "Other", "Electronics")

        "Música" -> listOf("Tickets & Events", "Books & Supplies", "Other")

        else -> listOf("Books & Supplies", "Electronics", "Other")
    }
}