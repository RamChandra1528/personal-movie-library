package com.example

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.CloudMovie
import com.example.data.EntertainmentType
import com.example.data.WatchStatus
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class FirestoreSyncRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun unauthenticatedUser_cannotWriteToVault() = runTest {
    auth.signOut()
    try {
      firestore.collection("users").document("user_unauth")
        .collection("movies").document("movie_1")
        .set(
          mapOf(
            "id" to "movie_1",
            "title" to "Inception",
            "userId" to "user_unauth"
          )
        ).await()
      fail("Expected PERMISSION_DENIED for unauthenticated write")
    } catch (e: Exception) {
      val fe = e.unwrapFirestoreException()
      assertNotNull("Expected FirebaseFirestoreException", fe)
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fe?.code)
    }
  }

  @Test
  fun authenticatedUser_cannotAccessOtherUserVault() = runTest {
    val aliceUid = signInTestUser("alice@cinevault.test")
    val bobUid = "bob_other_user_id"

    try {
      firestore.collection("users").document(bobUid)
        .collection("movies").document("movie_bob")
        .get().await()
      fail("Expected PERMISSION_DENIED for cross-user read")
    } catch (e: Exception) {
      val fe = e.unwrapFirestoreException()
      assertNotNull("Expected FirebaseFirestoreException", fe)
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fe?.code)
    }
  }

  @Test
  fun authenticatedUser_canWriteAndReadOwnVault() = runTest {
    val aliceUid = signInTestUser("alice_vault@cinevault.test")

    val movieDoc = firestore.collection("users").document(aliceUid)
      .collection("movies").document("movie_test_1")

    val writeData = mapOf(
      "id" to "movie_test_1",
      "title" to "Interstellar",
      "userId" to aliceUid,
      "releaseYear" to 2014,
      "personalRating" to 5.0,
      "watchStatus" to WatchStatus.COMPLETED,
      "contentType" to EntertainmentType.MOVIE,
      "isFavorite" to true,
      "createdAt" to Timestamp.now(),
      "updatedAt" to Timestamp.now()
    )

    movieDoc.set(writeData).await()

    val snapshot = movieDoc.get().await()
    assertTrue(snapshot.exists())
    assertEquals("Interstellar", snapshot.getString("title"))
    assertEquals(aliceUid, snapshot.getString("userId"))
  }

  @Test
  fun authenticatedUser_invalidMovieRejectedByRules() = runTest {
    val aliceUid = signInTestUser("alice_validation@cinevault.test")

    try {
      firestore.collection("users").document(aliceUid)
        .collection("movies").document("movie_invalid")
        .set(
          mapOf(
            "id" to "movie_invalid",
            "title" to "Over 5 Stars",
            "userId" to aliceUid,
            "personalRating" to 9.5, // exceeds 5.0 max
            "createdAt" to Timestamp.now(),
            "updatedAt" to Timestamp.now()
          )
        ).await()
      fail("Expected PERMISSION_DENIED for invalid movie rating")
    } catch (e: Exception) {
      val fe = e.unwrapFirestoreException()
      assertNotNull("Expected FirebaseFirestoreException", fe)
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fe?.code)
    }
  }
}
