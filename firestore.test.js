const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write movies", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("movies").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("movies").doc("movie_1").set({
    id: "movie_1",
    title: "Inception",
    userId: ALICE_UID,
    createdAt: new Date(),
    updatedAt: new Date()
  }));
});

test("Authenticated user cannot read another user's movies", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).collection("movies").doc("bob_movie").set({
      id: "bob_movie",
      title: "Interstellar",
      userId: BOB_UID,
      createdAt: new Date(),
      updatedAt: new Date()
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("movies").doc("bob_movie").get());
});

test("Alice can create a valid movie in her own vault", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("movies").doc("movie_1").set({
      id: "movie_1",
      title: "Interstellar",
      userId: ALICE_UID,
      releaseYear: 2014,
      personalRating: 4.5,
      watchStatus: "COMPLETED",
      contentType: "MOVIE",
      isFavorite: true,
      createdAt: new Date(),
      updatedAt: new Date()
    })
  );
});

test("Alice cannot create a movie with invalid rating (>5)", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("movies").doc("movie_2").set({
      id: "movie_2",
      title: "The Dark Knight",
      userId: ALICE_UID,
      personalRating: 10,
      createdAt: new Date(),
      updatedAt: new Date()
    })
  );
});

test("Alice can read her own movies collection", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await aliceDb.collection("users").doc(ALICE_UID).collection("movies").doc("movie_1").set({
    id: "movie_1",
    title: "Memento",
    userId: ALICE_UID,
    createdAt: new Date(),
    updatedAt: new Date()
  });

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("movies").get());
});

test("Alice can update user settings and search history", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("settings").doc("user_settings").set({
      userId: ALICE_UID,
      sortOrder: "TITLE_ASC",
      preferredType: "MOVIE",
      backupEnabled: true,
      updatedAt: new Date()
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("search_history").doc("search_1").set({
      id: "search_1",
      query: "Christopher Nolan",
      contentType: "MOVIE",
      timestamp: Date.now(),
      userId: ALICE_UID,
      createdAt: new Date()
    })
  );
});
