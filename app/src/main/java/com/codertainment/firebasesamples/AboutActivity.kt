package com.codertainment.firebasesamples

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.content.ContextCompat
import com.danielstone.materialaboutlibrary.ConvenienceBuilder
import com.danielstone.materialaboutlibrary.MaterialAboutActivity
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem
import com.danielstone.materialaboutlibrary.items.MaterialAboutTitleItem
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard
import com.danielstone.materialaboutlibrary.model.MaterialAboutList
import com.mikepenz.aboutlibraries.LibsBuilder
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic

class AboutActivity : MaterialAboutActivity() {

  override fun getActivityTitle() = "About this app"

  override fun getMaterialAboutList(p0: Context): MaterialAboutList {
    val mal = MaterialAboutList.Builder()


    val card1 = getCard()

    val title = MaterialAboutTitleItem.Builder()
        .text(R.string.app_name)
        .desc(R.string.app_description)
        .icon(R.mipmap.ic_launcher)
        .build()

    card1.addItem(title)

    val version = ConvenienceBuilder.createVersionActionItem(
        this,
        getIcon(GoogleMaterial.Icon.gmd_info_outline),
        "Version",
        true
    )
    card1.addItem(version)

    val licenses = MaterialAboutActionItem.Builder()
        .text("Open Source Licenses")
        .icon(getIcon(GoogleMaterial.Icon.gmd_book))
        .setOnClickAction {
          LibsBuilder()
              .withAboutIconShown(false)
              .withAboutVersionShown(false)
              .withAboutDescription("")
              .start(this)
        }
        .build()
    card1.addItem(licenses)


    val rate = ConvenienceBuilder.createRateActionItem(this, getIcon(MaterialDesignIconic.Icon.gmi_star), "Rate the app", null)
    card1.addItem(rate)

    val source = MaterialAboutActionItem.Builder()
        .text("View app source or contribute")
        .icon(getIcon(MaterialDesignIconic.Icon.gmi_github))
        .setOnClickAction {
          openLink("https://github.com/shripal17/Firebase-ML-Kit-Sample")
        }
        .build()
    card1.addItem(source)

    mal.addCard(card1.build())


    mal.addCard(getDeveloperCard("Shripal Jain", "App Developer and Android Enthusiast", "shripal17"))

    return mal.build()
  }

  private fun getDeveloperCard(name: String, role: String, githubUsername: String): MaterialAboutCard {
    val c = getCard()
    c.title("Author")
    val name = MaterialAboutActionItem.Builder()
        .text(name)
        .subText(role)
        .icon(getIcon(MaterialDesignIconic.Icon.gmi_account))
        .build()
    c.addItem(name)
    val github = MaterialAboutActionItem.Builder()
        .text("Fork on GitHub")
        .icon(getIcon(MaterialDesignIconic.Icon.gmi_github))
        .setOnClickAction {
          openLink("https://github.com/$githubUsername")
        }
        .build()
    c.addItem(github)

    return c.build()
  }

  private fun openLink(link: String) = startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))

  private fun getIcon(iicon: IIcon) = IconicsDrawable(this).icon(iicon).color(getIconColor())

  private fun getIconColor() = ContextCompat.getColor(this, R.color.colorAccent)

  private fun getCard() = MaterialAboutCard.Builder()

}
