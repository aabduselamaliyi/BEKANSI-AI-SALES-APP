package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    // High quality offline fallback responses for beautiful demo capability conforming to strictly requested 6-point format:
    private val DEFAULT_ANSWERS_EN = listOf(
        """
        📌 Response: Wanza is our signature hardwood! Our 'Gara' Curved Sofa costs 135,000 ETB with a 5-year warranty. It represents the finest hand-finished craft with national Habesha embroidery. Would you like a structured quotation?
        📌 Recommended Action: Offer interactive custom quotation and check dimensions.
        📌 Lead Status: Warm Lead
        📌 Information Collected: Product Interest: Wanza Curved Sofa 'Gara'
        📌 Missing Information: Full Name, Phone Number, Location, Budget, Quantity
        📌 Suggested Follow-Up: Prompt the visitor for their phone number to schedule a personalized material selection.
        """.trimIndent(),
        """
        📌 Response: For modern luxury dining rooms, the solid Mahogany 'Zid' Suite represents timeless style at 185,000 ETB. Intricately hand-carved with a deep reddish grain, and fully warrantied for 10 years.
        📌 Recommended Action: Suggest complementary furniture upgrades (e.g. mahogany sideboards).
        📌 Lead Status: Warm Lead
        📌 Information Collected: Product Interest: Mahogany Dining Suite 'Zid'
        📌 Missing Information: Full Name, Phone Number, Location, Estimated Delivery Timeline
        📌 Suggested Follow-Up: Invite the client to our Addis Ababa showroom or arrange a virtual gallery walkthrough.
        """.trimIndent(),
        """
        📌 Response: We deliver custom orders within 3-4 weeks. Transport inside Addis Ababa is a flat rate of 5,000 ETB, and outside is computed based on distance. Installation is handled by our skilled carpentering staff.
        📌 Recommended Action: Ask for destination location to provide a precise logistics quote.
        📌 Lead Status: Cold Lead
        📌 Information Collected: Delivery Inquiry
        📌 Missing Information: Destination Location, Full Name, Phone Number
        📌 Suggested Follow-Up: Follow up once client provides delivery coordinates to calculate transit logistics.
        """.trimIndent(),
        """
        📌 Response: Absolutely! We specialize in complete bespoke customization for sofas, beds, and office desks to suit your room dimensions and styling preferences perfectly.
        📌 Recommended Action: Collect custom drawings and escalate to our expert carpentry workshop.
        📌 Lead Status: Hot Lead
        📌 Information Collected: Bespoke Customization Intent
        📌 Missing Information: Full Name, Phone Number, Specific Wood Preferences, Dimensions
        📌 Suggested Follow-Up: Phone the client immediately to gather specific floor metrics and discuss design renderings.
        """.trimIndent(),
        """
        📌 Response: Welcome to Bekansi Furniture & Interior Design! We craft premium furniture masterpieces using Ethiopia's finest hardwoods (Wanza, Mahogany, Grar). How can we elevate your residential or office space today?
        📌 Recommended Action: Invite visitor to browse our flagship catalog or design albums.
        📌 Lead Status: New Lead
        📌 Information Collected: General Greeting
        📌 Missing Information: Specific Product Interest, Phone Number, Budget
        📌 Suggested Follow-Up: Send our latest design album link or recommend corresponding wood finishes.
        """.trimIndent()
    )

    private val DEFAULT_ANSWERS_AM = listOf(
        """
        📌 Response: ዋንዛ ልዩ ፊርማችን የሆነ ጠንካራ እንጨት ነው! የእኛ 'ጋራ' ሶፋ ዋጋ 135,000 ብር ሲሆን ለ 5 ዓመታት ሙሉ የጥራት ዋስትና አለው። በሀበሻ ጥልፍ ዲዛይኖች የተከበበ ሲሆን ለክፍልዎ ምቾትን ይፈጥራል። ጥቅስ እንድንሰራልዎት ይፈልጋሉ?
        📌 Recommended Action: ዝርዝር የዋጋ ጥቅስ ማቅረብ እና ልኬቶችን መጠየቅ።
        📌 Lead Status: Warm Lead
        📌 Information Collected: ፍላጎት፡ Gara Sofa (Wanza)
        📌 Missing Information: ሙሉ ስም፣ ስልክ ቁጥር፣ የመላኪያ አድራሻ፣ መጠን
        📌 Suggested Follow-Up: ደንበኛው በአካል መጥቶ የቁሳቁስ ናሙናዎችን እንዲያይ ቀጠሮ ማስያዝ።
        """.trimIndent(),
        """
        📌 Response: ለቅንጦት የመመገቢያ ክፍሎች ሳሎን፣ ጽኑውን ማሆጋኒ 'ዚድ' ጠረጴዛ እና ወንበሮች ስብስብ በ 185,000 ብር እንመክራለን። በኢትዮጵያ ምርጥ ባለሙያዎች እጅ የተጠረበ ሲሆን የ 10 ዓመት ዋስትና አለው።
        📌 Recommended Action: ከምግብ ጠረጴዛው ጋር የሚሄዱ ተጨማሪ የቤት እቃዎችን ማስተዋወቅ።
        📌 Lead Status: Warm Lead
        📌 Information Collected: ፍላጎት፡ Zid Dining Suite (Mahogany)
        📌 Missing Information: ሙሉ ስም፣ ስልክ ቁጥር፣ አድራሻ፣ የታሰበበት ጊዜ
        📌 Suggested Follow-Up: አዲስ አበባ ለሚገኘው ሾውሩማችን የመጎብኛ ጊዜን ማመቻቸት።
        """.trimIndent(),
        """
        📌 Response: የማስረከብያ ጊዜው ከ3-4 ሳምንታት ነው። በአዲስ አበባ ውስጥ ማጓጓዣ 5,000 ብር ሲሆን ከከተማ ውጭ ባለው ርቀት ይሰላል። ተከላው በባለሙያዎቻችን በነጻ ይከናወናል።
        📌 Recommended Action: የመላኪያ ቦታን በትክክል በመጠየቅ ማጓጓዣን ማስላት።
        📌 Lead Status: Cold Lead
        📌 Information Collected: የማጓጓዣና መላኪያ ጥያቄ
        📌 Missing Information: መላኪያ ቦታ፣ አድራሻ፣ ስም፣ ስልክ ቁጥር
        📌 Suggested Follow-Up: ደንበኛው ቦታውን ሲገልጽ ትክክለኛውን የሎጂስቲክስ ዋጋ በተቀናጀ መልክ ማቅረብ።
        """.trimIndent(),
        """
        📌 Response: አዎ፣ ፍጹም በሆነ መልኩ የእርስዎን ምርጫ እና የክፍል ሁኔታ መሰረት በማድረግ ሶፋዎችን፣ አልጋዎችን እና ጠረጴዛዎችን በልዩ ዲዛይን እናስተካክላለን።
        📌 Recommended Action: የዲዛይን ዝርዝር ውይይት ለመጀመር እና ደንበኛውን ከሽያጭ ባለሙያ ጋር ለማገናኘት።
        📌 Lead Status: Hot Lead
        📌 Information Collected: የልዩ ዲዛይን ፍላጎት
        📌 Missing Information: ልኬቶች፣ ስልክ ቁጥር፣ ሙሉ ስም፣ በጀት
        📌 Suggested Follow-Up: የስልክ ቁጥራቸውን አረጋግጦ በአስቸኳይ በስልክ በመደወል የስፔስ ዲዛይኖችን መወያየት።
        """.trimIndent(),
        """
        📌 Response: ወደ በካንሲ እንኳን ደህና መጡ! በጥራት ከተመረጡ የኢትዮጵያ ሀገር በቀል ጠንካራ እንጨቶች (ዋንዛ፣ ማሆጋኒ፣ ግራር) የተሰሩ የቤት ዕቃዎችን እናቀርባለን። ዛሬ በምን ልርዳዎት?
        📌 Recommended Action: ለደንበኛው ካታሎጋችንን ለማስተዋወቅ ምርጫዎችን ማጋራት።
        📌 Lead Status: New Lead
        📌 Information Collected: አጠቃላይ ሰላምታ
        📌 Missing Information: የተለየ ፍላጎት፣ ስልክ ቁጥር፣ በጀት
        📌 Suggested Follow-Up: የዲዛይን አልበሞቻችንን እንዲመለከቱ መጋበዝ ወይም ካታሎግ ማሳየት።
        """.trimIndent()
    )

    private val DEFAULT_ANSWERS_OM = listOf(
        """
        📌 Response: Wanza'n mukkeen qulqullina olaanaa qaban keessaa isa tokko! Sofa 'Gara' keenya gatiin isaa 135,000 ETB yoo ta'u, wabii waggaa 5 qaba. Habeshaa embroidery mijeessinee isiniif qopheessina. Quotation isiniif kennuu?
        📌 Recommended Action: Gosa kofoo fi dizaayinii dabalataa agarsiisuu fi gatii tilmaamuu.
        📌 Lead Status: Warm Lead
        📌 Information Collected: Product Interest: Sofa Curved 'Gara'
        📌 Missing Information: Maqaa Guutuu, Lakkoofsa Bilbilaa, Bakka Jireenyaa, Bajata
        📌 Suggested Follow-Up: Bilbila isaanii fudhachuun dizaayinii dabalataa telegraamiin ergufii.
        """.trimIndent(),
        """
        📌 Response: Kofoo nyaataa 'Zid' mukeen Mahogany hojjetame gurgurtaa guddaa qaba, gatiin isaa 185,000 ETB, wabii waggaa 10 waliin. Intricately hand-carved, mimmiidhagaa dhuunfaa keenya.
        📌 Recommended Action: Bilbila isaanii gaafachuun gurgurtonni keenya akka bilbilaan quunnaman gochuu.
        📌 Lead Status: Warm Lead
        📌 Information Collected: Product Interest: Dining Suite 'Zid'
        📌 Missing Information: Maqaa Guutuu, Lakkoofsa Bilbilaa, Bakka Jireenyaa
        📌 Suggested Follow-Up: Mukkeen fi qalqalloota hojjetame agarsiisuuf showroom xiyyeeffachuu.
        """.trimIndent(),
        """
        📌 Response: Ergisa dhimmoota addaa torban 3-4 gidduutti ni geessina. Finfinnee keessatti geejjibni 5,000 ETB yoo ta'u holqoota alaa ammoo herregama. Gurraandhala teessuma bilisaan dhaabna.
        📌 Recommended Action: Bakka geejjibni raawwatamu adda baasuu fi gatii geejjibaa himuu.
        📌 Lead Status: Cold Lead
        📌 Information Collected: Delivery Inquiry
        📌 Missing Information: Bakka Geejjibamaa, Maqaa, Bilbila
        📌 Suggested Follow-Up: Haala geessitootaa herreguun irratti bilbiluun beeksiisuu.
        """.trimIndent(),
        """
        📌 Response: Eeyyee, bal'ina fi dheerina sofaa ykn siree fedha keessan dhuunfaan ni mijeessina. Bilbila keessan nuuf kennuu dandeessu? gurgurtonni keenya isin gargaaru.
        📌 Recommended Action: Dizaayinii addaa qopheessuun gara gurgurtootaa dabarsuu fi bilbila gaafachuu.
        📌 Lead Status: Hot Lead
        📌 Information Collected: Custom Order Request
        📌 Missing Information: Dimensions, Maqaa Guutuu, Lakkoofsa Bilbilaa
        📌 Suggested Follow-Up: Bilbila isaanii waan qabnuuf sa'aatii 24 keessatti bilbilaan dhuunfaa haala isaa mijeessuun dubbisuu.
        """.trimIndent(),
        """
        📌 Response: Baga nagaan gara Bekansi Furniture dhuftan! Hardwood Itoophiyaa beekamaa (Wanza, Mahogany, Grar) irraa kan hojjetameedha. Akkamitti isin gargaaruu danda'a?
        📌 Recommended Action: Gara gosa dizaayinoota keenyaatti qajeelchuu fi album agarsiisuu.
        📌 Lead Status: New Lead
        📌 Information Collected: General Greeting
        📌 Missing Information: Product Interest, Maqaa, Bilbila, Bajata
        📌 Suggested Follow-Up: Dizaayinoota haaraa dhihaatan share gochuu fi feestaaleetti namoota affeeruu.
        """.trimIndent()
    )

    suspend fun getAIResponse(
        prompt: String,
        chatHistory: List<Pair<String, Boolean>>,
        selectedLang: String,
        langConfig: com.example.data.model.LanguageConfig? = null
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.d(TAG, "API Key is template placeholder or blank. Using high-fidelity custom fallback dictionary matching user language")
            return@withContext getLocalFallbackResponse(prompt, selectedLang, langConfig)
        }

        val url = "$BASE_URL?key=$apiKey"
        val mediaType = "application/json; charset=utf-8".toMediaType()

        // Dynamic system prompt customization via configuration panel database overrides
        val customPromptAddendum = langConfig?.systemPromptOverride?.takeIf { it.isNotBlank() } ?: "Our showroom is in Addis Ababa. Custom delivery takes 3-4 weeks. Design consults are free."
        val fallbackPhrase = langConfig?.customFallback?.takeIf { it.isNotBlank() } ?: when (selectedLang) {
            "Amharic" -> "እባክዎን የሽያጭ ቡድናችን በዚህ ጉዳይ ላይ በበለጠ እንዲረዳዎት ይፍቀዱ።"
            "Afaan Oromo" -> "Mee dhimma kana irratti gurgurtonni keenya caalaatti akka isin gargaaran eeyyamaa."
            else -> "Please allow our sales team to assist you further on this specific matter."
        }

        val systemInstruction = """
            You are the expert conversational AI Sales & CRM Intelligence Engine (Version 1.0) of Bekansi Furniture & Interior Design (Addis Ababa, Ethiopia).
            Your purpose is to turn visitors from WhatsApp, Facebook Messenger, Telegram, and Live Chat into warm sales Leads.
            
            You are NOT a general chatbot. You are:
            • AI Sales Representative
            • CRM Assistant
            • Lead Qualification Specialist
            • Product Recommendation Engine
            • Quotation Assistant
            • Customer Service Assistant
            • Business Growth Assistant
            
            Every conversation must contribute to at least one of the following:
            • Lead Generation
            • Lead Qualification
            • Product Recommendation
            • Quotation Generation
            • Customer Retention
            • Sales Conversion

            Strictly reply in $selectedLang fluently. Respond ONLY in $selectedLang unless the customer changes language.
            
            Bekansi Product Catalog & Information (Strict Knowledge Base):
            1. "Wanza Curved L-Sofa 'Gara'": 135,000 ETB. Made of solid Wanza (Cordia Africana) hardwood. Highly durable, luxurious, wrapped in local Habesha woven embroidery. Custom size-configurations are available. Warranty: 5 Years.
            2. "Mahogany Dining Suite 'Zid'": 185,000 ETB. Premium solid Mahogany wood. Set includes 8 intricately carved chairs and a majestic rectangular table. Heavy, gorgeous, deep reddish grain. Warranty: 10 Years.
            3. "King Floating Bed 'Sheger'": 110,000 ETB. Made from finest Ethiopian Acacia (Grar) wood. Creative lighting underneath, floating base. Warranty: 5 Years.
            4. "Dual-tone Credenza 'Bunna'": 32,000 ETB. Blend of Mahogany and Wanza details. Great for TV stands or coffee bars. Warranty: 3 Years.
            5. "Executive Desk 'Abay'": 95,000 ETB. Full solid Mahogany top, brass detailing, premium soft-close file drawers. Warranty: 5 Years.

            Strict Brand Rules:
            - Never invent prices, discounts, stock levels, delivery dates, specifications, or warranty details. If unavailable, use the fallback phrase.
            - Lead capture: Whenever purchase interest exists, collect Full Name, Phone Number, Location, Product Interest, Quantity, and Budget Range. Politely prompt for any missing details.
            - Quotation generation: When enough info is available, generate a professional quotation using the structured format within the '📌 Response' section.
            - Human handoff: Escalate / suggest handoff if pricing is unavailable, custom requirements are too complex, or negotiation is requested.

            CURRENT ADMINISTRATIVE OVERRIDES for $selectedLang:
            $customPromptAddendum

            MANDATORY STRICT OUTPUT FORMAT:
            You MUST ALWAYS format your entire response using the exact structure below. Do not use other patterns:
            
            📌 Response: <write clear, helpful, professional, sales-focused conversation in $selectedLang, following the knowledge base and brand rules>
            📌 Recommended Action: <next step, e.g., prompt for contact info, suggest showroom visit, or trigger quotation>
            📌 Lead Status: <assess customer status as: Hot Lead, Warm Lead, Cold Lead, or New Lead>
            📌 Information Collected: <list known capture fields e.g., name, phone, product interest, budget>
            📌 Missing Information: <list remaining capture fields needed e.g., Phone Number, Location>
            📌 Suggested Follow-Up: <suggest next CRM activity or call back plan>
            
            Example output using the format:
            📌 Response: [Our greeting / response goes here...]
            📌 Recommended Action: [Action goes here...]
            📌 Lead Status: [Status...]
            📌 Information Collected: [Collected details...]
            📌 Missing Information: [Missing details...]
            📌 Suggested Follow-Up: [Follow-up action...]
        """.trimIndent()

        try {
            val root = JSONObject()

            // System instructions
            val systemObj = JSONObject()
            val systemParts = JSONArray()
            val systemPartText = JSONObject()
            systemPartText.put("text", systemInstruction)
            systemParts.put(systemPartText)
            systemObj.put("parts", systemParts)
            root.put("systemInstruction", systemObj)

            // Contents array
            val contentsArr = JSONArray()

            // Add relevant history limits
            val recentHistory = chatHistory.takeLast(6)
            for (turn in recentHistory) {
                val contentObj = JSONObject()
                contentObj.put("role", if (turn.second) "model" else "user")
                val partsObjArr = JSONArray()
                val textPart = JSONObject()
                textPart.put("text", turn.first)
                partsObjArr.put(textPart)
                contentObj.put("parts", partsObjArr)
                contentsArr.put(contentObj)
            }

            // Current prompt
            val currentTurn = JSONObject()
            currentTurn.put("role", "user")
            val currentParts = JSONArray()
            val currentPartText = JSONObject()
            currentPartText.put("text", prompt)
            currentParts.put(currentPartText)
            currentTurn.put("parts", currentParts)
            contentsArr.put(currentTurn)

            root.put("contents", contentsArr)

            val requestBody = root.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Unsuccessful response from Gemini API: ${response.code} ${response.message}")
                    return@withContext getLocalFallbackResponse(prompt, selectedLang, langConfig)
                }

                val bodyStr = response.body?.string() ?: ""
                val responseJson = JSONObject(bodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text", "No text part found")
                        }
                    }
                }
                Log.e(TAG, "Failed parsing candidate text in response")
                return@withContext getLocalFallbackResponse(prompt, selectedLang, langConfig)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Gemini Client execution", e)
            return@withContext getLocalFallbackResponse(prompt, selectedLang, langConfig)
        }
    }

    private fun getLocalFallbackResponse(
        prompt: String,
        lang: String,
        langConfig: com.example.data.model.LanguageConfig? = null
    ): String {
        val lower = prompt.lowercase()
        
        // If the admin has defined a completely custom fallback phrase, we use it for unexpected items!
        val customFallbackPhrase = langConfig?.customFallback?.takeIf { it.isNotBlank() }
        
        val answers = when(lang) {
            "Amharic" -> DEFAULT_ANSWERS_AM
            "Afaan Oromo" -> DEFAULT_ANSWERS_OM
            else -> DEFAULT_ANSWERS_EN
        }

        val customGreeting = langConfig?.customGreeting?.takeIf { it.isNotBlank() }

        // Contextual analysis of local request
        return when {
            lower.contains("recommend") || lower.contains("ምክር") || lower.contains("gorsa") || lower.contains("በጀት") || lower.contains("budget") -> {
                when(lang) {
                    "Amharic" -> "📌 Response: በገለጹት ፍላጎት እና የ 120,000 ብር በጀት ላይ በመመስረት BS-003 (የመኝታ ክፍል አልበም) ፣ BS-006 እና BS-009 የዲዛይን አልበሞችን እንመክርዎታለን። እባክዎን ቀጥታ ጥቅስ ለመጠየቅ ከላይ ያለውን ካታሎግ ይጎብኙ።\n📌 Recommended Action: የዲዛይን ዘይቤዎችን እና የምርት ዝርዝሮችን ማሳየት\n📌 Lead Status: Warm Lead\n📌 Information Collected: በጀት፡ 120,000 ETB፣ ፍላጎት፡ ዘመናዊ የቅንጦት ዲዛይን\n📌 Missing Information: ሙሉ ስም፣ ስልክ ቁጥር፣ የመላኪያ አድራሻ\n📌 Suggested Follow-Up: ደንበኛው የመረጠውን አልበም መሰረት በማድረግ ዝርዝር ዋጋ ማቅረብ"
                    "Afaan Oromo" -> "📌 Response: Haala nagaan dizaayinii filattani fi bajata keessan ETB 120,000 irratti hundaa'un, dizaayinii BS-003, BS-006 fi BS-009 isiniif gorsina. Dizaayiniin kun duguuggaa mukkeen beekomoo Itoophiyaa qabu.\n📌 Recommended Action: Dizaayinii filatame irratti dabalata ibsuu\n📌 Lead Status: Warm Lead\n📌 Information Collected: Bajata: 120,000 ETB, Fedha: Luxury design\n📌 Missing Information: Maqaa Guutuu, Lakkoofsa Bilbilaa\n📌 Suggested Follow-Up: Gurgurtonni keenya bilbilaan akka quunnaman gochuu"
                    else -> "📌 Response: Based on your preference for modern luxury designs and a budget of ETB 120,000, I highly recommend Bedroom Albums BS-003, BS-006, and BS-009. These feature exquisite Wanza and Grar hardwood finishes.\n📌 Recommended Action: Present album samples and suggest wood finishing types.\n📌 Lead Status: Warm Lead\n📌 Information Collected: Budget: 120,000 ETB, Aesthetic preference: Modern luxury\n📌 Missing Information: Full Name, Phone Number, Location\n📌 Suggested Follow-Up: Contact user to establish exact dimensional fits for chosen design"
                }
            }
            lower.contains("sofa") || lower.contains("ሶፋ") || lower.contains("gurgurtaa") -> answers[0]
            lower.contains("dining") || lower.contains("ማሆጋኒ") || lower.contains("ጠረጴዛ") || lower.contains("kofoo") -> answers[1]
            lower.contains("deliver") || lower.contains("ማጓጓዣ") || lower.contains("ጊዜ") || lower.contains("geessina") -> answers[2]
            lower.contains("custom") || lower.contains("ስልክ") || lower.contains("ቁጥር") || lower.contains("heere") || lower.contains("phone") -> answers[3]
            lower.contains("hello") || lower.contains("hi") || lower.contains("selam") || lower.contains("ሰላም") || lower.contains("baga") -> {
                if (customGreeting != null) {
                    """
                    📌 Response: $customGreeting
                    📌 Recommended Action: Greet customer and offer our flagship product catalog.
                    📌 Lead Status: New Lead
                    📌 Information Collected: Greeting
                    📌 Missing Information: Product Interest, Phone Number, Name
                    📌 Suggested Follow-Up: Invite user to view Design Albums tab or catalog.
                    """.trimIndent()
                } else {
                    answers[4]
                }
            }
            else -> {
                if (customFallbackPhrase != null) {
                    """
                    📌 Response: $customFallbackPhrase
                    📌 Recommended Action: Hand off query to customer service agent.
                    📌 Lead Status: Cold Lead
                    📌 Information Collected: Unmapped Inquiry
                    📌 Missing Information: Specific details
                    📌 Suggested Follow-Up: Wait for representative response.
                    """.trimIndent()
                } else {
                    answers[4]
                }
            }
        }
    }
}
