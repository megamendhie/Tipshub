package com.sqube.tipshub.adapters

import com.sqube.tipshub.adapters.TipsAdapter.TipsHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sqube.tipshub.R
import com.sqube.tipshub.databinding.ItemGameTipBinding
import com.sqube.tipshub.models.GameTip
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class TipsAdapter(private val tips: ArrayList<GameTip>) : RecyclerView.Adapter<TipsHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TipsHolder {
        val binding = ItemGameTipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TipsHolder(binding)
    }

    override fun onBindViewHolder(holder: TipsHolder, position: Int) {
        holder.bindItems(tips[position])
    }

    override fun getItemCount(): Int {
        return tips.size
    }

    inner class TipsHolder(val binding: ItemGameTipBinding) : RecyclerView.ViewHolder(binding.root) {
        private val imgFlag = binding.imgFlag

        private fun setFlag(country: String) {
            when (country) {
                "Champions League", "Europa League", "Nations League", "World Cup" -> imgFlag.setImageResource(R.drawable.flag_world)
                "Ascension Island", "Tristan da Cunha", "Diego Garcia", "United Kingdom" -> imgFlag.setImageResource(R.drawable.flag_united_kingdom)
                "Northern Ireland" -> imgFlag.setImageResource(R.drawable.flag_northern_ireland)
                "Wales" -> imgFlag.setImageResource(R.drawable.flag_wales)
                "England" -> imgFlag.setImageResource(R.drawable.flag_england)
                "Scotland" -> imgFlag.setImageResource(R.drawable.flag_scotland)
                "Andorra" -> imgFlag.setImageResource(R.drawable.flag_andorra)
                "UAE" -> imgFlag.setImageResource(R.drawable.flag_uae)
                "Afghanistan" -> imgFlag.setImageResource(R.drawable.flag_afghanistan)
                "Antigua & Barbuda" -> imgFlag.setImageResource(R.drawable.flag_antigua_and_barbuda)
                "Anguilla" -> imgFlag.setImageResource(R.drawable.flag_anguilla)
                "Albania" -> imgFlag.setImageResource(R.drawable.flag_albania)
                "Armenia" -> imgFlag.setImageResource(R.drawable.flag_armenia)
                "Angola" -> imgFlag.setImageResource(R.drawable.flag_angola)
                "Antarctica" -> imgFlag.setImageResource(R.drawable.flag_antarctica)
                "Argentina" -> imgFlag.setImageResource(R.drawable.flag_argentina)
                "American Samoa" -> imgFlag.setImageResource(R.drawable.flag_american_samoa)
                "Austria" -> imgFlag.setImageResource(R.drawable.flag_austria)
                "Australia", "Heard & McDonald Islands" -> imgFlag.setImageResource(R.drawable.flag_australia)
                "Aruba" -> imgFlag.setImageResource(R.drawable.flag_aruba)
                "Åland Islands" -> imgFlag.setImageResource(R.drawable.flag_aland)
                "Azerbaijan" -> imgFlag.setImageResource(R.drawable.flag_azerbaijan)
                "Bosnia & Herzegovina" -> imgFlag.setImageResource(R.drawable.flag_bosnia)
                "Barbados" -> imgFlag.setImageResource(R.drawable.flag_barbados)
                "Bangladesh" -> imgFlag.setImageResource(R.drawable.flag_bangladesh)
                "Belgium" -> imgFlag.setImageResource(R.drawable.flag_belgium)
                "Burkina Faso" -> imgFlag.setImageResource(R.drawable.flag_burkina_faso)
                "Bulgaria" -> imgFlag.setImageResource(R.drawable.flag_bulgaria)
                "Bahrain" -> imgFlag.setImageResource(R.drawable.flag_bahrain)
                "Burundi" -> imgFlag.setImageResource(R.drawable.flag_burundi)
                "Benin" -> imgFlag.setImageResource(R.drawable.flag_benin)
                "St. Barthélemy" -> imgFlag.setImageResource(R.drawable.flag_saint_barthelemy)
                "Bermuda" -> imgFlag.setImageResource(R.drawable.flag_bermuda)
                "Brunei" -> imgFlag.setImageResource(R.drawable.flag_brunei)
                "Bolivia" -> imgFlag.setImageResource(R.drawable.flag_bolivia)
                "Caribbean Netherlands" -> imgFlag.setImageResource(R.drawable.flag_netherlands_antilles)
                "Brazil" -> imgFlag.setImageResource(R.drawable.flag_brazil)
                "Bahamas" -> imgFlag.setImageResource(R.drawable.flag_bahamas)
                "Bhutan" -> imgFlag.setImageResource(R.drawable.flag_bhutan)
                "Bouvet Island", "Norway", "Svalbard & Jan Mayen" -> imgFlag.setImageResource(R.drawable.flag_norway)
                "Botswana" -> imgFlag.setImageResource(R.drawable.flag_botswana)
                "Belarus" -> imgFlag.setImageResource(R.drawable.flag_belarus)
                "Belize" -> imgFlag.setImageResource(R.drawable.flag_belize)
                "Canada" -> imgFlag.setImageResource(R.drawable.flag_canada)
                "Cocos (Keeling) Islands" -> imgFlag.setImageResource(R.drawable.flag_cocos)
                "Congo - Kinshasa" -> imgFlag.setImageResource(R.drawable.flag_democratic_republic_of_the_congo)
                "Central African Republic" -> imgFlag.setImageResource(R.drawable.flag_central_african_republic)
                "Congo - Brazzaville" -> imgFlag.setImageResource(R.drawable.flag_republic_of_the_congo)
                "Switzerland" -> imgFlag.setImageResource(R.drawable.flag_switzerland)
                "Côte d’Ivoire" -> imgFlag.setImageResource(R.drawable.flag_cote_divoire)
                "Cook Islands" -> imgFlag.setImageResource(R.drawable.flag_cook_islands)
                "Chile" -> imgFlag.setImageResource(R.drawable.flag_chile)
                "Cameroon" -> imgFlag.setImageResource(R.drawable.flag_cameroon)
                "China" -> imgFlag.setImageResource(R.drawable.flag_china)
                "Colombia" -> imgFlag.setImageResource(R.drawable.flag_colombia)
                "Clipperton Island", "France", "Mayotte", "French Southern Territories", "Réunion", "French Guiana" -> imgFlag.setImageResource(R.drawable.flag_france)
                "Costa Rica" -> imgFlag.setImageResource(R.drawable.flag_costa_rica)
                "Cuba" -> imgFlag.setImageResource(R.drawable.flag_cuba)
                "Cape Verde" -> imgFlag.setImageResource(R.drawable.flag_cape_verde)
                "Curaçao" -> imgFlag.setImageResource(R.drawable.flag_curacao)
                "Christmas Island" -> imgFlag.setImageResource(R.drawable.flag_christmas_island)
                "Cyprus" -> imgFlag.setImageResource(R.drawable.flag_cyprus)
                "Czech Republic" -> imgFlag.setImageResource(R.drawable.flag_czech_republic)
                "Germany" -> imgFlag.setImageResource(R.drawable.flag_germany)
                "Djibouti" -> imgFlag.setImageResource(R.drawable.flag_djibouti)
                "Denmark" -> imgFlag.setImageResource(R.drawable.flag_denmark)
                "Dominica" -> imgFlag.setImageResource(R.drawable.flag_dominica)
                "Dominican Republic" -> imgFlag.setImageResource(R.drawable.flag_dominican_republic)
                "Algeria" -> imgFlag.setImageResource(R.drawable.flag_algeria)
                "Ceuta & Melilla", "Spain" -> imgFlag.setImageResource(R.drawable.flag_spain)
                "Ecuador" -> imgFlag.setImageResource(R.drawable.flag_ecuador)
                "Estonia" -> imgFlag.setImageResource(R.drawable.flag_estonia)
                "Egypt" -> imgFlag.setImageResource(R.drawable.flag_egypt)
                "Western Sahara", "Morocco" -> imgFlag.setImageResource(R.drawable.flag_morocco)
                "Eritrea" -> imgFlag.setImageResource(R.drawable.flag_eritrea)
                "Ethiopia" -> imgFlag.setImageResource(R.drawable.flag_ethiopia)
                "Finland" -> imgFlag.setImageResource(R.drawable.flag_finland)
                "Fiji" -> imgFlag.setImageResource(R.drawable.flag_fiji)
                "Falkland Islands" -> imgFlag.setImageResource(R.drawable.flag_falkland_islands)
                "Micronesia" -> imgFlag.setImageResource(R.drawable.flag_micronesia)
                "Faroe Islands" -> imgFlag.setImageResource(R.drawable.flag_faroe_islands)
                "Gabon" -> imgFlag.setImageResource(R.drawable.flag_gabon)
                "Grenada" -> imgFlag.setImageResource(R.drawable.flag_grenada)
                "Georgia" -> imgFlag.setImageResource(R.drawable.flag_georgia)
                "Guernsey" -> imgFlag.setImageResource(R.drawable.flag_guernsey)
                "Ghana" -> imgFlag.setImageResource(R.drawable.flag_ghana)
                "Gibraltar" -> imgFlag.setImageResource(R.drawable.flag_gibraltar)
                "Greenland" -> imgFlag.setImageResource(R.drawable.flag_greenland)
                "Gambia" -> imgFlag.setImageResource(R.drawable.flag_gambia)
                "Guinea" -> imgFlag.setImageResource(R.drawable.flag_guinea)
                "Guadeloupe" -> imgFlag.setImageResource(R.drawable.flag_guadeloupe)
                "Equatorial Guinea" -> imgFlag.setImageResource(R.drawable.flag_equatorial_guinea)
                "Greece" -> imgFlag.setImageResource(R.drawable.flag_greece)
                "South Georgia & South Sandwich Islands" -> imgFlag.setImageResource(R.drawable.flag_south_georgia)
                "Guatemala" -> imgFlag.setImageResource(R.drawable.flag_guatemala)
                "Guam" -> imgFlag.setImageResource(R.drawable.flag_guam)
                "Guinea-Bissau" -> imgFlag.setImageResource(R.drawable.flag_guinea_bissau)
                "Guyana" -> imgFlag.setImageResource(R.drawable.flag_guyana)
                "Hong Kong SAR China" -> imgFlag.setImageResource(R.drawable.flag_hong_kong)
                "Honduras" -> imgFlag.setImageResource(R.drawable.flag_honduras)
                "Croatia" -> imgFlag.setImageResource(R.drawable.flag_croatia)
                "Haiti" -> imgFlag.setImageResource(R.drawable.flag_haiti)
                "Hungary" -> imgFlag.setImageResource(R.drawable.flag_hungary)
                "Canary Islands" -> imgFlag.setImageResource(R.drawable.flag_spain)
                "Indonesia" -> imgFlag.setImageResource(R.drawable.flag_indonesia)
                "Ireland" -> imgFlag.setImageResource(R.drawable.flag_ireland)
                "Israel" -> imgFlag.setImageResource(R.drawable.flag_israel)
                "Isle of Man" -> imgFlag.setImageResource(R.drawable.flag_isleof_man)
                "India" -> imgFlag.setImageResource(R.drawable.flag_india)
                "British Indian Ocean Territory" -> imgFlag.setImageResource(R.drawable.flag_british_indian_ocean_territory)
                "Iraq" -> imgFlag.setImageResource(R.drawable.flag_iraq)
                "Iran" -> imgFlag.setImageResource(R.drawable.flag_iran)
                "Iceland" -> imgFlag.setImageResource(R.drawable.flag_iceland)
                "Italy" -> imgFlag.setImageResource(R.drawable.flag_italy)
                "Jersey" -> imgFlag.setImageResource(R.drawable.flag_jersey)
                "Jamaica" -> imgFlag.setImageResource(R.drawable.flag_jamaica)
                "Jordan" -> imgFlag.setImageResource(R.drawable.flag_jordan)
                "Japan" -> imgFlag.setImageResource(R.drawable.flag_japan)
                "Kenya" -> imgFlag.setImageResource(R.drawable.flag_kenya)
                "Kyrgyzstan" -> imgFlag.setImageResource(R.drawable.flag_kyrgyzstan)
                "Cambodia" -> imgFlag.setImageResource(R.drawable.flag_cambodia)
                "Kiribati" -> imgFlag.setImageResource(R.drawable.flag_kiribati)
                "Comoros" -> imgFlag.setImageResource(R.drawable.flag_comoros)
                "St. Kitts & Nevis" -> imgFlag.setImageResource(R.drawable.flag_saint_kitts_and_nevis)
                "North Korea" -> imgFlag.setImageResource(R.drawable.flag_north_korea)
                "South Korea" -> imgFlag.setImageResource(R.drawable.flag_south_korea)
                "Kuwait" -> imgFlag.setImageResource(R.drawable.flag_kuwait)
                "Cayman Islands" -> imgFlag.setImageResource(R.drawable.flag_cayman_islands)
                "Kazakhstan" -> imgFlag.setImageResource(R.drawable.flag_kazakhstan)
                "Laos" -> imgFlag.setImageResource(R.drawable.flag_laos)
                "Lebanon" -> imgFlag.setImageResource(R.drawable.flag_lebanon)
                "St. Lucia" -> imgFlag.setImageResource(R.drawable.flag_saint_lucia)
                "Liechtenstein" -> imgFlag.setImageResource(R.drawable.flag_liechtenstein)
                "Sri Lanka" -> imgFlag.setImageResource(R.drawable.flag_sri_lanka)
                "Liberia" -> imgFlag.setImageResource(R.drawable.flag_liberia)
                "Lesotho" -> imgFlag.setImageResource(R.drawable.flag_lesotho)
                "Lithuania" -> imgFlag.setImageResource(R.drawable.flag_lithuania)
                "Luxembourg" -> imgFlag.setImageResource(R.drawable.flag_luxembourg)
                "Latvia" -> imgFlag.setImageResource(R.drawable.flag_latvia)
                "Libya" -> imgFlag.setImageResource(R.drawable.flag_libya)
                "Monaco" -> imgFlag.setImageResource(R.drawable.flag_monaco)
                "Moldova" -> imgFlag.setImageResource(R.drawable.flag_moldova)
                "Montenegro" -> imgFlag.setImageResource(R.drawable.flag_of_montenegro)
                "St. Martin" -> imgFlag.setImageResource(R.drawable.flag_saint_martin)
                "Madagascar" -> imgFlag.setImageResource(R.drawable.flag_madagascar)
                "Marshall Islands" -> imgFlag.setImageResource(R.drawable.flag_marshall_islands)
                "Macedonia" -> imgFlag.setImageResource(R.drawable.flag_macedonia)
                "Mali" -> imgFlag.setImageResource(R.drawable.flag_mali)
                "Myanmar (Burma)" -> imgFlag.setImageResource(R.drawable.flag_myanmar)
                "Mongolia" -> imgFlag.setImageResource(R.drawable.flag_mongolia)
                "Macau SAR China" -> imgFlag.setImageResource(R.drawable.flag_macao)
                "Northern Mariana Islands" -> imgFlag.setImageResource(R.drawable.flag_northern_mariana_islands)
                "Martinique" -> imgFlag.setImageResource(R.drawable.flag_martinique)
                "Mauritania" -> imgFlag.setImageResource(R.drawable.flag_mauritania)
                "Montserrat" -> imgFlag.setImageResource(R.drawable.flag_montserrat)
                "Malta" -> imgFlag.setImageResource(R.drawable.flag_malta)
                "Mauritius" -> imgFlag.setImageResource(R.drawable.flag_mauritius)
                "Maldives" -> imgFlag.setImageResource(R.drawable.flag_maldives)
                "Malawi" -> imgFlag.setImageResource(R.drawable.flag_malawi)
                "Mexico" -> imgFlag.setImageResource(R.drawable.flag_mexico)
                "Malaysia" -> imgFlag.setImageResource(R.drawable.flag_malaysia)
                "Mozambique" -> imgFlag.setImageResource(R.drawable.flag_mozambique)
                "Namibia" -> imgFlag.setImageResource(R.drawable.flag_namibia)
                "New Caledonia" -> imgFlag.setImageResource(R.drawable.flag_new_caledonia)
                "Niger" -> imgFlag.setImageResource(R.drawable.flag_niger)
                "Norfolk Island" -> imgFlag.setImageResource(R.drawable.flag_norfolk_island)
                "Nigeria" -> imgFlag.setImageResource(R.drawable.flag_nigeria)
                "Nicaragua" -> imgFlag.setImageResource(R.drawable.flag_nicaragua)
                "Netherlands" -> imgFlag.setImageResource(R.drawable.flag_netherlands)
                "Nepal" -> imgFlag.setImageResource(R.drawable.flag_nepal)
                "Nauru" -> imgFlag.setImageResource(R.drawable.flag_nauru)
                "Niue" -> imgFlag.setImageResource(R.drawable.flag_niue)
                "New Zealand" -> imgFlag.setImageResource(R.drawable.flag_new_zealand)
                "Oman" -> imgFlag.setImageResource(R.drawable.flag_oman)
                "Panama" -> imgFlag.setImageResource(R.drawable.flag_panama)
                "Peru" -> imgFlag.setImageResource(R.drawable.flag_peru)
                "French Polynesia" -> imgFlag.setImageResource(R.drawable.flag_french_polynesia)
                "Papua New Guinea" -> imgFlag.setImageResource(R.drawable.flag_papua_new_guinea)
                "Philippines" -> imgFlag.setImageResource(R.drawable.flag_philippines)
                "Pakistan" -> imgFlag.setImageResource(R.drawable.flag_pakistan)
                "Poland" -> imgFlag.setImageResource(R.drawable.flag_poland)
                "St. Pierre & Miquelon" -> imgFlag.setImageResource(R.drawable.flag_saint_pierre)
                "Pitcairn Islands" -> imgFlag.setImageResource(R.drawable.flag_pitcairn_islands)
                "Puerto Rico" -> imgFlag.setImageResource(R.drawable.flag_puerto_rico)
                "Palestinian Territories" -> imgFlag.setImageResource(R.drawable.flag_palestine)
                "Portugal" -> imgFlag.setImageResource(R.drawable.flag_portugal)
                "Palau" -> imgFlag.setImageResource(R.drawable.flag_palau)
                "Paraguay" -> imgFlag.setImageResource(R.drawable.flag_paraguay)
                "Qatar" -> imgFlag.setImageResource(R.drawable.flag_qatar)
                "Romania" -> imgFlag.setImageResource(R.drawable.flag_romania)
                "Serbia" -> imgFlag.setImageResource(R.drawable.flag_serbia)
                "Russia" -> imgFlag.setImageResource(R.drawable.flag_russian_federation)
                "Rwanda" -> imgFlag.setImageResource(R.drawable.flag_rwanda)
                "Saudi Arabia" -> imgFlag.setImageResource(R.drawable.flag_saudi_arabia)
                "Solomon Islands" -> imgFlag.setImageResource(R.drawable.flag_soloman_islands)
                "Seychelles" -> imgFlag.setImageResource(R.drawable.flag_seychelles)
                "Sudan" -> imgFlag.setImageResource(R.drawable.flag_sudan)
                "Sweden" -> imgFlag.setImageResource(R.drawable.flag_sweden)
                "Singapore" -> imgFlag.setImageResource(R.drawable.flag_singapore)
                "St. Helena" -> imgFlag.setImageResource(R.drawable.flag_saint_helena)
                "Slovenia" -> imgFlag.setImageResource(R.drawable.flag_slovenia)
                "Slovakia" -> imgFlag.setImageResource(R.drawable.flag_slovakia)
                "Sierra Leone" -> imgFlag.setImageResource(R.drawable.flag_sierra_leone)
                "San Marino" -> imgFlag.setImageResource(R.drawable.flag_san_marino)
                "Senegal" -> imgFlag.setImageResource(R.drawable.flag_senegal)
                "Somalia" -> imgFlag.setImageResource(R.drawable.flag_somalia)
                "Suriname" -> imgFlag.setImageResource(R.drawable.flag_suriname)
                "South Sudan" -> imgFlag.setImageResource(R.drawable.flag_south_sudan)
                "São Tomé & Príncipe" -> imgFlag.setImageResource(R.drawable.flag_sao_tome_and_principe)
                "El Salvador" -> imgFlag.setImageResource(R.drawable.flag_el_salvador)
                "Sint Maarten" -> imgFlag.setImageResource(R.drawable.flag_sint_maarten)
                "Syria" -> imgFlag.setImageResource(R.drawable.flag_syria)
                "Swaziland" -> imgFlag.setImageResource(R.drawable.flag_swaziland)
                "Turks & Caicos Islands" -> imgFlag.setImageResource(R.drawable.flag_turks_and_caicos_islands)
                "Chad" -> imgFlag.setImageResource(R.drawable.flag_chad)
                "Togo" -> imgFlag.setImageResource(R.drawable.flag_togo)
                "Thailand" -> imgFlag.setImageResource(R.drawable.flag_thailand)
                "Tajikistan" -> imgFlag.setImageResource(R.drawable.flag_tajikistan)
                "Tokelau" -> imgFlag.setImageResource(R.drawable.flag_tokelau)
                "Timor-Leste" -> imgFlag.setImageResource(R.drawable.flag_timor_leste)
                "Turkmenistan" -> imgFlag.setImageResource(R.drawable.flag_turkmenistan)
                "Tunisia" -> imgFlag.setImageResource(R.drawable.flag_tunisia)
                "Tonga" -> imgFlag.setImageResource(R.drawable.flag_tonga)
                "Turkey" -> imgFlag.setImageResource(R.drawable.flag_turkey)
                "Trinidad & Tobago" -> imgFlag.setImageResource(R.drawable.flag_trinidad_and_tobago)
                "Tuvalu" -> imgFlag.setImageResource(R.drawable.flag_tuvalu)
                "Taiwan" -> imgFlag.setImageResource(R.drawable.flag_taiwan)
                "Tanzania" -> imgFlag.setImageResource(R.drawable.flag_tanzania)
                "Ukraine" -> imgFlag.setImageResource(R.drawable.flag_ukraine)
                "Uganda" -> imgFlag.setImageResource(R.drawable.flag_uganda)
                "U.S. Outlying Islands", "USA" -> imgFlag.setImageResource(R.drawable.flag_united_states_of_america)
                "Uruguay" -> imgFlag.setImageResource(R.drawable.flag_uruguay)
                "Uzbekistan" -> imgFlag.setImageResource(R.drawable.flag_uzbekistan)
                "Vatican City" -> imgFlag.setImageResource(R.drawable.flag_vatican_city)
                "St. Vincent & Grenadines" -> imgFlag.setImageResource(R.drawable.flag_saint_vicent_and_the_grenadines)
                "Venezuela" -> imgFlag.setImageResource(R.drawable.flag_venezuela)
                "British Virgin Islands" -> imgFlag.setImageResource(R.drawable.flag_british_virgin_islands)
                "U.S. Virgin Islands" -> imgFlag.setImageResource(R.drawable.flag_us_virgin_islands)
                "Vietnam" -> imgFlag.setImageResource(R.drawable.flag_vietnam)
                "Vanuatu" -> imgFlag.setImageResource(R.drawable.flag_vanuatu)
                "Wallis & Futuna" -> imgFlag.setImageResource(R.drawable.flag_wallis_and_futuna)
                "Samoa" -> imgFlag.setImageResource(R.drawable.flag_samoa)
                "Kosovo" -> imgFlag.setImageResource(R.drawable.flag_kosovo)
                "Yemen" -> imgFlag.setImageResource(R.drawable.flag_yemen)
                "South Africa" -> imgFlag.setImageResource(R.drawable.flag_south_africa)
                "Zambia" -> imgFlag.setImageResource(R.drawable.flag_zambia)
                "Zimbabwe" -> imgFlag.setImageResource(R.drawable.flag_zimbabwe)
                else -> imgFlag.setImageResource(R.color.white)
            }
        }

        fun bindItems(tip: GameTip) {
            with(binding){
                val region = tip.region + "  -"
                txtLeague.text = tip.league
                txtRegion.text = region
                txtPrediction.text = tip.prediction
                txtHomeTeam.text = tip.homeTeam
                txtAwayTeam.text = tip.awayTeam
                txtResult.text = if (tip.result.isEmpty()) "vs" else tip.result
                txtProbability.text = String.format(Locale.getDefault(), "%.2f%%", 100 * tip.probability)
                txtTime.text = getFormattedTime(tip.time)
                if (tip.status == "lost") imgStatus.visibility = View.INVISIBLE else {
                    imgStatus.setImageResource(if (tip.status == "pending") R.drawable.ic_hourglass_empty_color_24dp else R.drawable.ic_check_circle_green_24dp)
                }
                imgStatus.visibility = if (tip.status == "lost") View.INVISIBLE else View.VISIBLE
                setFlag(tip.region)
            }
        }

        private fun getFormattedTime(time: String): String {
            val oldFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
            oldFormat.timeZone = TimeZone.getTimeZone("GMT")
            val newFormatter = SimpleDateFormat("dd MMM - hh:mma", Locale.ENGLISH)
            newFormatter.timeZone = TimeZone.getDefault()
            var date: Date? = null
            try {
                date = oldFormat.parse(time)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            return if (date == null) "" else {
                var dateTime = newFormatter.format(date)
                dateTime = dateTime.replace("PM", "pm")
                dateTime = dateTime.replace("AM", "am")
                dateTime
            }
        }
    }
}