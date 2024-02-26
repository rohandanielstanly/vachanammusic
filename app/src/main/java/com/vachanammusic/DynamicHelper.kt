package com.vachanammusic

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.DynamicLink.AndroidParameters
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import java.net.URLDecoder
import java.util.regex.Pattern

// import com.google.android.gms.tasks.Task;
object DynamicHelper {
    // change to your domain prefix
    const val DOMAIN_PREFIX = "https://vachanammusic.page.link"

    // Retrieve post ID and type from a Firebase Dynamic Link
    fun retrieveLink(intent: Intent?, cxt: Context?, listener: RetrievalListener) {
        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent!!)
            .addOnSuccessListener(
                (cxt as Activity?)!!
            ) { pendingDynamicLinkData ->
                // Get deep link from result (may be null if no link is found)
                var deepLink: Uri? = null
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                }
                if (deepLink != null) {
                    // String postID = deepLink.getLastPathSegment();
                    // String postType = deepLink.getPathSegments().get(0);
                    // Utils.showMessage(cxt,  deepLink.getPathSegments().toString());
                    listener.onPostRetrieved(deepLink.pathSegments.toString())
                }
            }
            .addOnFailureListener(
                cxt!!
            ) { listener.onPostRetrievalFailed("Something went wrong! restart to continue.") }

        /*
         try{
         String postID = dynamicLink.getLastPathSegment();
         String postType = dynamicLink.getPathSegments().get(0);
         listener.onPostRetrieved(postID, postType);
         }catch(Exception e){
         String str = extractLink(dynamicLink.toString());
         if(str == null) {
         listener.onPostRetrievalFailed("Something went wrong!");
         return;
         }
         retrieveLink(Uri.parse(str), listener);
         } */
    }

    fun shareLink(
        context: FragmentActivity,
        shareType: String,
        shareID: String?,
        songCover: String,
        songTitle: String,
        songDescription: String
    ) {
        Toast.makeText(context, "Generating link", Toast.LENGTH_SHORT).show()

        val dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setDomainUriPrefix(DOMAIN_PREFIX)
            .setLink(Uri.parse(DOMAIN_PREFIX + "/" + shareType + "/" + Uri.parse(shareID).lastPathSegment.toString()))
            .setAndroidParameters(AndroidParameters.Builder(context.packageName).build())
            .setSocialMetaTagParameters(
                DynamicLink.SocialMetaTagParameters.Builder()
                    .setTitle(songTitle)
                    .setDescription(songDescription)
                    .setImageUrl(Uri.parse(songCover))
                    .build()
            )
            .buildDynamicLink()

        FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLongLink(dynamicLink.uri)
            .buildShortDynamicLink()
            .addOnCompleteListener(context) { task ->
                if (task.isSuccessful) {
                    val i2 = Intent("android.intent.action.SEND")
                    i2.type = "text/plain"
                    i2.putExtra("android.intent.extra.TEXT", task.result.shortLink.toString())
                    context.startActivity(Intent.createChooser(i2, "Share"))
                } else {
                    Toast.makeText(context, "Generating link failed!", Toast.LENGTH_SHORT).show()
                }
            }

    }

    fun extractLink(url: String?): String? {
        val linkRegex = "link=(.+)"
        val pattern = Pattern.compile(linkRegex)
        val matcher = pattern.matcher(url)
        return if (matcher.find()) {
            val linkValue = matcher.group(1)
            try {
                URLDecoder.decode(linkValue, "UTF-8")
            } catch (e: Exception) {
                // handle URL decoding exception
                e.printStackTrace()
                null
            }
        } else {
            // "link" parameter not found in URL
            null
        }
    }

    // Interface for handling post retrieval
    interface RetrievalListener {
        fun onPostRetrieved(path: String?)
        fun onPostRetrievalFailed(error: String?)
    } /*
	//Your app name and firebase package name must be same
	if (!key.equals("")) {
	Util.showMessage(getApplicationContext(), "Generating");
	dyn = FirebaseDynamicLinks.getInstance().createDynamicLink()
	.setLink(Uri.parse("https://indosw.com/jcoderz.php?id=".concat(key)))
	.setDomainUriPrefix("codebox.page.link")
	.setAndroidParameters(
	new DynamicLink.AndroidParameters.Builder(getPackageName())
	.setMinimumVersion(1)
	.build())
	.setSocialMetaTagParameters(
	new DynamicLink.SocialMetaTagParameters.Builder()
	.setTitle(textview1.getText().toString())
	.setDescription(textview2.getText().toString())
	.build())
	.buildDynamicLink();
	Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
	.setLongLink(Uri.parse("https://".concat(dyn.getUri().toString())))
	.buildShortDynamicLink()
	.addOnCompleteListener(ViewActivity.this, new OnCompleteListener<ShortDynamicLink>() {
	@Override
	public void onComplete(@NonNull Task<ShortDynamicLink> task) {
	if (task.isSuccessful()) {
	// Short link created
	Uri shortLink = task.getResult().getShortLink();
	Uri flowchartLink = task.getResult().getPreviewLink();

	final String srtLink = shortLink.toString();
	final String flwLink = shortLink.toString();

	Intent intentDynLink = new Intent();
	intentDynLink.setAction(Intent.ACTION_SEND);
	intentDynLink.putExtra(Intent.EXTRA_TEXT,  srtLink);
	intentDynLink.setType("text/plain");
	startActivity(intentDynLink);
	Util.showMessage(getApplicationContext(), "Link Generated");




	} else {
	// Error


	Util.showMessage(getApplicationContext(), "Failed to generate link");


	}
	}
	});
	} */
}
