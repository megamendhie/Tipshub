package adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sqube.tipshub.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;
import models.GameTip;

public class TipsAdapter extends RecyclerView.Adapter<TipsAdapter.TipsHolder> {
    private ArrayList<GameTip> tips;

    public TipsAdapter(ArrayList<GameTip> tips){
        this.tips = tips;
    }

    @NonNull
    @Override
    public TipsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(parent.getContext()).inflate(R.layout.game_tip_view, parent, false);
        return new TipsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TipsHolder holder, int position) {
        GameTip gameTip = tips.get(position);
        holder.bindItems(gameTip);
    }

    @Override
    public int getItemCount() {
        return tips.size();
    }

    class TipsHolder extends RecyclerView.ViewHolder{
        TextView txtRegion, txtLeague, txtTime, txtHomeTeam, txtAwayTeam, txtResult, txtPrediction, txtProbabity;
        ImageView imgStatus, imgFlag;
        TipsHolder(@NonNull View itemView) {
            super(itemView);

            txtAwayTeam =itemView.findViewById(R.id.txtAwayTeam);
            txtHomeTeam =itemView.findViewById(R.id.txtHomeTeam);
            txtLeague =itemView.findViewById(R.id.txtLeague);
            txtPrediction = itemView.findViewById(R.id.txtPrediction);
            txtProbabity = itemView.findViewById(R.id.txtProbability);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtRegion = itemView.findViewById(R.id.txtRegion);
            txtResult = itemView.findViewById(R.id.txtResult);
            imgStatus = itemView.findViewById(R.id.imgStatus);
            imgFlag = itemView.findViewById(R.id.imgFlag);
        }

        private void setFlag(String country) {
            switch (country) {
                case "Ascension Island":
                case "Tristan da Cunha":
                case "Diego Garcia":
                case "United Kingdom":
                    imgFlag.setImageResource(R.drawable.flag_united_kingdom); break;
                case "Northern Ireland":
                    imgFlag.setImageResource(R.drawable.flag_northern_ireland); break;
                case "Wales":
                    imgFlag.setImageResource(R.drawable.flag_wales); break;
                case "England":
                    imgFlag.setImageResource(R.drawable.flag_england); break;
                case "Scotland":
                    imgFlag.setImageResource(R.drawable.flag_scotland); break;
                case "Andorra":
                    imgFlag.setImageResource(R.drawable.flag_andorra); break;
                case "United Arab Emirates":
                    imgFlag.setImageResource(R.drawable.flag_uae); break;
                case "Afghanistan":
                    imgFlag.setImageResource(R.drawable.flag_afghanistan); break;
                case "Antigua & Barbuda":
                    imgFlag.setImageResource(R.drawable.flag_antigua_and_barbuda); break;
                case "Anguilla":
                    imgFlag.setImageResource(R.drawable.flag_anguilla); break;
                case "Albania":
                    imgFlag.setImageResource(R.drawable.flag_albania); break;
                case "Armenia":
                    imgFlag.setImageResource(R.drawable.flag_armenia); break;
                case "Angola":
                    imgFlag.setImageResource(R.drawable.flag_angola); break;
                case "Antarctica":
                    imgFlag.setImageResource(R.drawable.flag_antarctica); break;
                case "Argentina":
                    imgFlag.setImageResource(R.drawable.flag_argentina); break;
                case "American Samoa":
                    imgFlag.setImageResource(R.drawable.flag_american_samoa); break;
                case "Austria":
                    imgFlag.setImageResource(R.drawable.flag_austria); break;
                case "Australia":
                case "Heard & McDonald Islands":
                    imgFlag.setImageResource(R.drawable.flag_australia); break;
                case "Aruba":
                    imgFlag.setImageResource(R.drawable.flag_aruba); break;
                case "Åland Islands":
                    imgFlag.setImageResource(R.drawable.flag_aland); break;
                case "Azerbaijan":
                    imgFlag.setImageResource(R.drawable.flag_azerbaijan); break;
                case "Bosnia & Herzegovina":
                    imgFlag.setImageResource(R.drawable.flag_bosnia); break;
                case "Barbados":
                    imgFlag.setImageResource(R.drawable.flag_barbados); break;
                case "Bangladesh":
                    imgFlag.setImageResource(R.drawable.flag_bangladesh); break;
                case "Belgium":
                    imgFlag.setImageResource(R.drawable.flag_belgium); break;
                case "Burkina Faso":
                    imgFlag.setImageResource(R.drawable.flag_burkina_faso); break;
                case "Bulgaria":
                    imgFlag.setImageResource(R.drawable.flag_bulgaria); break;
                case "Bahrain":
                    imgFlag.setImageResource(R.drawable.flag_bahrain); break;
                case "Burundi":
                    imgFlag.setImageResource(R.drawable.flag_burundi); break;
                case "Benin":
                    imgFlag.setImageResource(R.drawable.flag_benin); break;
                case "St. Barthélemy":
                    imgFlag.setImageResource(R.drawable.flag_saint_barthelemy); break;
                case "Bermuda":
                    imgFlag.setImageResource(R.drawable.flag_bermuda); break;
                case "Brunei":
                    imgFlag.setImageResource(R.drawable.flag_brunei); break;
                case "Bolivia":
                    imgFlag.setImageResource(R.drawable.flag_bolivia); break;
                case "Caribbean Netherlands":
                    imgFlag.setImageResource(R.drawable.flag_netherlands_antilles); break;
                case "Brazil":
                    imgFlag.setImageResource(R.drawable.flag_brazil); break;
                case "Bahamas":
                    imgFlag.setImageResource(R.drawable.flag_bahamas); break;
                case "Bhutan":
                    imgFlag.setImageResource(R.drawable.flag_bhutan); break;
                case "Bouvet Island":
                case "Norway":
                case "Svalbard & Jan Mayen":
                    imgFlag.setImageResource(R.drawable.flag_norway); break;
                case "Botswana":
                    imgFlag.setImageResource(R.drawable.flag_botswana); break;
                case "Belarus":
                    imgFlag.setImageResource(R.drawable.flag_belarus); break;
                case "Belize":
                    imgFlag.setImageResource(R.drawable.flag_belize); break;
                case "Canada":
                    imgFlag.setImageResource(R.drawable.flag_canada); break;
                case "Cocos (Keeling) Islands":
                    imgFlag.setImageResource(R.drawable.flag_cocos); break;
                case "Congo - Kinshasa":
                    imgFlag.setImageResource(R.drawable.flag_democratic_republic_of_the_congo); break;
                case "Central African Republic":
                    imgFlag.setImageResource(R.drawable.flag_central_african_republic); break;
                case "Congo - Brazzaville":
                    imgFlag.setImageResource(R.drawable.flag_republic_of_the_congo); break;
                case "Switzerland":
                    imgFlag.setImageResource(R.drawable.flag_switzerland); break;
                case "Côte d’Ivoire":
                    imgFlag.setImageResource(R.drawable.flag_cote_divoire); break;
                case "Cook Islands":
                    imgFlag.setImageResource(R.drawable.flag_cook_islands); break;
                case "Chile":
                    imgFlag.setImageResource(R.drawable.flag_chile); break;
                case "Cameroon":
                    imgFlag.setImageResource(R.drawable.flag_cameroon); break;
                case "China":
                    imgFlag.setImageResource(R.drawable.flag_china); break;
                case "Colombia":
                    imgFlag.setImageResource(R.drawable.flag_colombia); break;
                case "Clipperton Island":
                case "France":
                case "Mayotte":
                case "French Southern Territories":
                case "Réunion":
                case "French Guiana":
                    imgFlag.setImageResource(R.drawable.flag_france); break;
                case "Costa Rica":
                    imgFlag.setImageResource(R.drawable.flag_costa_rica); break;
                case "Cuba":
                    imgFlag.setImageResource(R.drawable.flag_cuba); break;
                case "Cape Verde":
                    imgFlag.setImageResource(R.drawable.flag_cape_verde); break;
                case "Curaçao":
                    imgFlag.setImageResource(R.drawable.flag_curacao); break;
                case "Christmas Island":
                    imgFlag.setImageResource(R.drawable.flag_christmas_island); break;
                case "Cyprus":
                    imgFlag.setImageResource(R.drawable.flag_cyprus); break;
                case "Czechia":
                    imgFlag.setImageResource(R.drawable.flag_czech_republic); break;
                case "Germany":
                    imgFlag.setImageResource(R.drawable.flag_germany); break;
                case "Djibouti":
                    imgFlag.setImageResource(R.drawable.flag_djibouti); break;
                case "Denmark":
                    imgFlag.setImageResource(R.drawable.flag_denmark); break;
                case "Dominica":
                    imgFlag.setImageResource(R.drawable.flag_dominica); break;
                case "Dominican Republic":
                    imgFlag.setImageResource(R.drawable.flag_dominican_republic); break;
                case "Algeria":
                    imgFlag.setImageResource(R.drawable.flag_algeria); break;
                case "Ceuta & Melilla":
                case "Spain":
                    imgFlag.setImageResource(R.drawable.flag_spain); break;
                case "Ecuador":
                    imgFlag.setImageResource(R.drawable.flag_ecuador); break;
                case "Estonia":
                    imgFlag.setImageResource(R.drawable.flag_estonia); break;
                case "Egypt":
                    imgFlag.setImageResource(R.drawable.flag_egypt); break;
                case "Western Sahara":
                case "Morocco":
                    imgFlag.setImageResource(R.drawable.flag_morocco); break;
                case "Eritrea":
                    imgFlag.setImageResource(R.drawable.flag_eritrea); break;
                case "Ethiopia":
                    imgFlag.setImageResource(R.drawable.flag_ethiopia); break;
                case "Finland":
                    imgFlag.setImageResource(R.drawable.flag_finland); break;
                case "Fiji":
                    imgFlag.setImageResource(R.drawable.flag_fiji); break;
                case "Falkland Islands":
                    imgFlag.setImageResource(R.drawable.flag_falkland_islands); break;
                case "Micronesia":
                    imgFlag.setImageResource(R.drawable.flag_micronesia); break;
                case "Faroe Islands":
                    imgFlag.setImageResource(R.drawable.flag_faroe_islands); break;
                case "Gabon":
                    imgFlag.setImageResource(R.drawable.flag_gabon); break;
                case "Grenada":
                    imgFlag.setImageResource(R.drawable.flag_grenada); break;
                case "Georgia":
                    imgFlag.setImageResource(R.drawable.flag_georgia); break;
                case "Guernsey":
                    imgFlag.setImageResource(R.drawable.flag_guernsey); break;
                case "Ghana":
                    imgFlag.setImageResource(R.drawable.flag_ghana); break;
                case "Gibraltar":
                    imgFlag.setImageResource(R.drawable.flag_gibraltar); break;
                case "Greenland":
                    imgFlag.setImageResource(R.drawable.flag_greenland); break;
                case "Gambia":
                    imgFlag.setImageResource(R.drawable.flag_gambia); break;
                case "Guinea":
                    imgFlag.setImageResource(R.drawable.flag_guinea); break;
                case "Guadeloupe":
                    imgFlag.setImageResource(R.drawable.flag_guadeloupe); break;
                case "Equatorial Guinea":
                    imgFlag.setImageResource(R.drawable.flag_equatorial_guinea); break;
                case "Greece":
                    imgFlag.setImageResource(R.drawable.flag_greece); break;
                case "South Georgia & South Sandwich Islands":
                    imgFlag.setImageResource(R.drawable.flag_south_georgia); break;
                case "Guatemala":
                    imgFlag.setImageResource(R.drawable.flag_guatemala); break;
                case "Guam":
                    imgFlag.setImageResource(R.drawable.flag_guam); break;
                case "Guinea-Bissau":
                    imgFlag.setImageResource(R.drawable.flag_guinea_bissau); break;
                case "Guyana":
                    imgFlag.setImageResource(R.drawable.flag_guyana); break;
                case "Hong Kong SAR China":
                    imgFlag.setImageResource(R.drawable.flag_hong_kong); break;
                case "Honduras":
                    imgFlag.setImageResource(R.drawable.flag_honduras); break;
                case "Croatia":
                    imgFlag.setImageResource(R.drawable.flag_croatia); break;
                case "Haiti":
                    imgFlag.setImageResource(R.drawable.flag_haiti); break;
                case "Hungary":
                    imgFlag.setImageResource(R.drawable.flag_hungary); break;
                case "Canary Islands":
                    imgFlag.setImageResource(R.drawable.flag_spain); break;
                case "Indonesia":
                    imgFlag.setImageResource(R.drawable.flag_indonesia); break;
                case "Ireland":
                    imgFlag.setImageResource(R.drawable.flag_ireland); break;
                case "Israel":
                    imgFlag.setImageResource(R.drawable.flag_israel); break;
                case "Isle of Man":
                    imgFlag.setImageResource(R.drawable.flag_isleof_man); break;
                case "India":
                    imgFlag.setImageResource(R.drawable.flag_india); break;
                case "British Indian Ocean Territory":
                    imgFlag.setImageResource(R.drawable.flag_british_indian_ocean_territory); break;
                case "Iraq":
                    imgFlag.setImageResource(R.drawable.flag_iraq); break;
                case "Iran":
                    imgFlag.setImageResource(R.drawable.flag_iran); break;
                case "Iceland":
                    imgFlag.setImageResource(R.drawable.flag_iceland); break;
                case "Italy":
                    imgFlag.setImageResource(R.drawable.flag_italy); break;
                case "Jersey":
                    imgFlag.setImageResource(R.drawable.flag_jersey); break;
                case "Jamaica":
                    imgFlag.setImageResource(R.drawable.flag_jamaica); break;
                case "Jordan":
                    imgFlag.setImageResource(R.drawable.flag_jordan); break;
                case "Japan":
                    imgFlag.setImageResource(R.drawable.flag_japan); break;
                case "Kenya":
                    imgFlag.setImageResource(R.drawable.flag_kenya); break;
                case "Kyrgyzstan":
                    imgFlag.setImageResource(R.drawable.flag_kyrgyzstan); break;
                case "Cambodia":
                    imgFlag.setImageResource(R.drawable.flag_cambodia); break;
                case "Kiribati":
                    imgFlag.setImageResource(R.drawable.flag_kiribati); break;
                case "Comoros":
                    imgFlag.setImageResource(R.drawable.flag_comoros); break;
                case "St. Kitts & Nevis":
                    imgFlag.setImageResource(R.drawable.flag_saint_kitts_and_nevis); break;
                case "North Korea":
                    imgFlag.setImageResource(R.drawable.flag_north_korea); break;
                case "South Korea":
                    imgFlag.setImageResource(R.drawable.flag_south_korea); break;
                case "Kuwait":
                    imgFlag.setImageResource(R.drawable.flag_kuwait); break;
                case "Cayman Islands":
                    imgFlag.setImageResource(R.drawable.flag_cayman_islands); break;
                case "Kazakhstan":
                    imgFlag.setImageResource(R.drawable.flag_kazakhstan); break;
                case "Laos":
                    imgFlag.setImageResource(R.drawable.flag_laos); break;
                case "Lebanon":
                    imgFlag.setImageResource(R.drawable.flag_lebanon); break;
                case "St. Lucia":
                    imgFlag.setImageResource(R.drawable.flag_saint_lucia); break;
                case "Liechtenstein":
                    imgFlag.setImageResource(R.drawable.flag_liechtenstein); break;
                case "Sri Lanka":
                    imgFlag.setImageResource(R.drawable.flag_sri_lanka); break;
                case "Liberia":
                    imgFlag.setImageResource(R.drawable.flag_liberia); break;
                case "Lesotho":
                    imgFlag.setImageResource(R.drawable.flag_lesotho); break;
                case "Lithuania":
                    imgFlag.setImageResource(R.drawable.flag_lithuania); break;
                case "Luxembourg":
                    imgFlag.setImageResource(R.drawable.flag_luxembourg); break;
                case "Latvia":
                    imgFlag.setImageResource(R.drawable.flag_latvia); break;
                case "Libya":
                    imgFlag.setImageResource(R.drawable.flag_libya); break;
                case "Monaco":
                    imgFlag.setImageResource(R.drawable.flag_monaco); break;
                case "Moldova":
                    imgFlag.setImageResource(R.drawable.flag_moldova); break;
                case "Montenegro":
                    imgFlag.setImageResource(R.drawable.flag_of_montenegro); break;
                case "St. Martin":
                    imgFlag.setImageResource(R.drawable.flag_saint_martin); break;
                case "Madagascar":
                    imgFlag.setImageResource(R.drawable.flag_madagascar); break;
                case "Marshall Islands":
                    imgFlag.setImageResource(R.drawable.flag_marshall_islands); break;
                case "Macedonia":
                    imgFlag.setImageResource(R.drawable.flag_macedonia); break;
                case "Mali":
                    imgFlag.setImageResource(R.drawable.flag_mali); break;
                case "Myanmar (Burma)":
                    imgFlag.setImageResource(R.drawable.flag_myanmar); break;
                case "Mongolia":
                    imgFlag.setImageResource(R.drawable.flag_mongolia); break;
                case "Macau SAR China":
                    imgFlag.setImageResource(R.drawable.flag_macao); break;
                case "Northern Mariana Islands":
                    imgFlag.setImageResource(R.drawable.flag_northern_mariana_islands); break;
                case "Martinique":
                    imgFlag.setImageResource(R.drawable.flag_martinique); break;
                case "Mauritania":
                    imgFlag.setImageResource(R.drawable.flag_mauritania); break;
                case "Montserrat":
                    imgFlag.setImageResource(R.drawable.flag_montserrat); break;
                case "Malta":
                    imgFlag.setImageResource(R.drawable.flag_malta); break;
                case "Mauritius":
                    imgFlag.setImageResource(R.drawable.flag_mauritius); break;
                case "Maldives":
                    imgFlag.setImageResource(R.drawable.flag_maldives); break;
                case "Malawi":
                    imgFlag.setImageResource(R.drawable.flag_malawi); break;
                case "Mexico":
                    imgFlag.setImageResource(R.drawable.flag_mexico); break;
                case "Malaysia":
                    imgFlag.setImageResource(R.drawable.flag_malaysia); break;
                case "Mozambique":
                    imgFlag.setImageResource(R.drawable.flag_mozambique); break;
                case "Namibia":
                    imgFlag.setImageResource(R.drawable.flag_namibia); break;
                case "New Caledonia":
                    imgFlag.setImageResource(R.drawable.flag_new_caledonia); break;
                case "Niger":
                    imgFlag.setImageResource(R.drawable.flag_niger); break;
                case "Norfolk Island":
                    imgFlag.setImageResource(R.drawable.flag_norfolk_island); break;
                case "Nigeria":
                    imgFlag.setImageResource(R.drawable.flag_nigeria); break;
                case "Nicaragua":
                    imgFlag.setImageResource(R.drawable.flag_nicaragua); break;
                case "Netherlands":
                    imgFlag.setImageResource(R.drawable.flag_netherlands); break;
                case "Nepal":
                    imgFlag.setImageResource(R.drawable.flag_nepal); break;
                case "Nauru":
                    imgFlag.setImageResource(R.drawable.flag_nauru); break;
                case "Niue":
                    imgFlag.setImageResource(R.drawable.flag_niue); break;
                case "New Zealand":
                    imgFlag.setImageResource(R.drawable.flag_new_zealand); break;
                case "Oman":
                    imgFlag.setImageResource(R.drawable.flag_oman); break;
                case "Panama":
                    imgFlag.setImageResource(R.drawable.flag_panama); break;
                case "Peru":
                    imgFlag.setImageResource(R.drawable.flag_peru); break;
                case "French Polynesia":
                    imgFlag.setImageResource(R.drawable.flag_french_polynesia); break;
                case "Papua New Guinea":
                    imgFlag.setImageResource(R.drawable.flag_papua_new_guinea); break;
                case "Philippines":
                    imgFlag.setImageResource(R.drawable.flag_philippines); break;
                case "Pakistan":
                    imgFlag.setImageResource(R.drawable.flag_pakistan); break;
                case "Poland":
                    imgFlag.setImageResource(R.drawable.flag_poland); break;
                case "St. Pierre & Miquelon":
                    imgFlag.setImageResource(R.drawable.flag_saint_pierre); break;
                case "Pitcairn Islands":
                    imgFlag.setImageResource(R.drawable.flag_pitcairn_islands); break;
                case "Puerto Rico":
                    imgFlag.setImageResource(R.drawable.flag_puerto_rico); break;
                case "Palestinian Territories":
                    imgFlag.setImageResource(R.drawable.flag_palestine); break;
                case "Portugal":
                    imgFlag.setImageResource(R.drawable.flag_portugal); break;
                case "Palau":
                    imgFlag.setImageResource(R.drawable.flag_palau); break;
                case "Paraguay":
                    imgFlag.setImageResource(R.drawable.flag_paraguay); break;
                case "Qatar":
                    imgFlag.setImageResource(R.drawable.flag_qatar); break;
                case "Romania":
                    imgFlag.setImageResource(R.drawable.flag_romania); break;
                case "Serbia":
                    imgFlag.setImageResource(R.drawable.flag_serbia); break;
                case "Russia":
                    imgFlag.setImageResource(R.drawable.flag_russian_federation); break;
                case "Rwanda":
                    imgFlag.setImageResource(R.drawable.flag_rwanda); break;
                case "Saudi Arabia":
                    imgFlag.setImageResource(R.drawable.flag_saudi_arabia); break;
                case "Solomon Islands":
                    imgFlag.setImageResource(R.drawable.flag_soloman_islands); break;
                case "Seychelles":
                    imgFlag.setImageResource(R.drawable.flag_seychelles); break;
                case "Sudan":
                    imgFlag.setImageResource(R.drawable.flag_sudan); break;
                case "Sweden":
                    imgFlag.setImageResource(R.drawable.flag_sweden); break;
                case "Singapore":
                    imgFlag.setImageResource(R.drawable.flag_singapore); break;
                case "St. Helena":
                    imgFlag.setImageResource(R.drawable.flag_saint_helena); break;
                case "Slovenia":
                    imgFlag.setImageResource(R.drawable.flag_slovenia); break;
                case "Slovakia":
                    imgFlag.setImageResource(R.drawable.flag_slovakia); break;
                case "Sierra Leone":
                    imgFlag.setImageResource(R.drawable.flag_sierra_leone); break;
                case "San Marino":
                    imgFlag.setImageResource(R.drawable.flag_san_marino); break;
                case "Senegal":
                    imgFlag.setImageResource(R.drawable.flag_senegal); break;
                case "Somalia":
                    imgFlag.setImageResource(R.drawable.flag_somalia); break;
                case "Suriname":
                    imgFlag.setImageResource(R.drawable.flag_suriname); break;
                case "South Sudan":
                    imgFlag.setImageResource(R.drawable.flag_south_sudan); break;
                case "São Tomé & Príncipe":
                    imgFlag.setImageResource(R.drawable.flag_sao_tome_and_principe); break;
                case "El Salvador":
                    imgFlag.setImageResource(R.drawable.flag_el_salvador); break;
                case "Sint Maarten":
                    imgFlag.setImageResource(R.drawable.flag_sint_maarten); break;
                case "Syria":
                    imgFlag.setImageResource(R.drawable.flag_syria); break;
                case "Swaziland":
                    imgFlag.setImageResource(R.drawable.flag_swaziland); break;
                case "Turks & Caicos Islands":
                    imgFlag.setImageResource(R.drawable.flag_turks_and_caicos_islands); break;
                case "Chad":
                    imgFlag.setImageResource(R.drawable.flag_chad); break;
                case "Togo":
                    imgFlag.setImageResource(R.drawable.flag_togo); break;
                case "Thailand":
                    imgFlag.setImageResource(R.drawable.flag_thailand); break;
                case "Tajikistan":
                    imgFlag.setImageResource(R.drawable.flag_tajikistan); break;
                case "Tokelau":
                    imgFlag.setImageResource(R.drawable.flag_tokelau); break;
                case "Timor-Leste":
                    imgFlag.setImageResource(R.drawable.flag_timor_leste); break;
                case "Turkmenistan":
                    imgFlag.setImageResource(R.drawable.flag_turkmenistan); break;
                case "Tunisia":
                    imgFlag.setImageResource(R.drawable.flag_tunisia); break;
                case "Tonga":
                    imgFlag.setImageResource(R.drawable.flag_tonga); break;
                case "Turkey":
                    imgFlag.setImageResource(R.drawable.flag_turkey); break;
                case "Trinidad & Tobago":
                    imgFlag.setImageResource(R.drawable.flag_trinidad_and_tobago); break;
                case "Tuvalu":
                    imgFlag.setImageResource(R.drawable.flag_tuvalu); break;
                case "Taiwan":
                    imgFlag.setImageResource(R.drawable.flag_taiwan); break;
                case "Tanzania":
                    imgFlag.setImageResource(R.drawable.flag_tanzania); break;
                case "Ukraine":
                    imgFlag.setImageResource(R.drawable.flag_ukraine); break;
                case "Uganda":
                    imgFlag.setImageResource(R.drawable.flag_uganda); break;
                case "U.S. Outlying Islands":
                case "USA":
                    imgFlag.setImageResource(R.drawable.flag_united_states_of_america); break;
                case "Uruguay":
                    imgFlag.setImageResource(R.drawable.flag_uruguay); break;
                case "Uzbekistan":
                    imgFlag.setImageResource(R.drawable.flag_uzbekistan); break;
                case "Vatican City":
                    imgFlag.setImageResource(R.drawable.flag_vatican_city); break;
                case "St. Vincent & Grenadines":
                    imgFlag.setImageResource(R.drawable.flag_saint_vicent_and_the_grenadines); break;
                case "Venezuela":
                    imgFlag.setImageResource(R.drawable.flag_venezuela); break;
                case "British Virgin Islands":
                    imgFlag.setImageResource(R.drawable.flag_british_virgin_islands); break;
                case "U.S. Virgin Islands":
                    imgFlag.setImageResource(R.drawable.flag_us_virgin_islands); break;
                case "Vietnam":
                    imgFlag.setImageResource(R.drawable.flag_vietnam); break;
                case "Vanuatu":
                    imgFlag.setImageResource(R.drawable.flag_vanuatu); break;
                case "Wallis & Futuna":
                    imgFlag.setImageResource(R.drawable.flag_wallis_and_futuna); break;
                case "Samoa":
                    imgFlag.setImageResource(R.drawable.flag_samoa); break;
                case "Kosovo":
                    imgFlag.setImageResource(R.drawable.flag_kosovo); break;
                case "Yemen":
                    imgFlag.setImageResource(R.drawable.flag_yemen); break;
                case "South Africa":
                    imgFlag.setImageResource(R.drawable.flag_south_africa); break;
                case "Zambia":
                    imgFlag.setImageResource(R.drawable.flag_zambia); break;
                case "Zimbabwe":
                    imgFlag.setImageResource(R.drawable.flag_zimbabwe); break;
                default:
                    imgFlag.setImageResource(R.color.white); break;
            }
        }

        private void bindItems(GameTip tip){
            txtLeague.setText(tip.getLeague());
            String region = tip.getRegion() + "  -";
            txtRegion.setText(region);
            txtPrediction.setText(tip.getPrediction());
            txtHomeTeam.setText(tip.getHomeTeam());
            txtAwayTeam.setText(tip.getAwayTeam());
            txtResult.setText(tip.getResult().isEmpty()? "vs": tip.getResult());
            txtProbabity.setText(String.format(Locale.getDefault(),"%.2f%%", 100*tip.getProbability()));
            txtTime.setText(getFormattedTime(tip.getTime()));
            if(tip.getStatus().equals("lost"))
                imgStatus.setVisibility(View.INVISIBLE);
            else{
                imgStatus.setImageResource(tip.getStatus().equals("pending")?
                        R.drawable.ic_hourglass_empty_color_24dp: R.drawable.ic_check_circle_green_24dp);
            }
            imgStatus.setVisibility(tip.getStatus().equals("lost")? View.INVISIBLE: View.VISIBLE);
            setFlag(tip.getRegion());
        }

        private String getFormattedTime(String time){
            SimpleDateFormat oldFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
            oldFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            SimpleDateFormat newFormatter = new SimpleDateFormat("dd MMM - hh:mma", Locale.ENGLISH);
            newFormatter.setTimeZone(TimeZone.getDefault());
            Date date = null;
            try {
                date = oldFormat.parse(time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if(date==null)
                return "";
            else {
                String dateTime = newFormatter.format(date);
                dateTime = dateTime.replace("PM", "pm");
                dateTime = dateTime.replace("AM", "am");
                return dateTime;
            }
        }
    }
}
