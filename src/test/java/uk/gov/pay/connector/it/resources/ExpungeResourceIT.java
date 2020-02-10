package uk.gov.pay.connector.it.resources;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.connector.app.ConnectorApp;
import uk.gov.pay.connector.charge.model.domain.ChargeStatus;
import uk.gov.pay.connector.expunge.service.LedgerStub;
import uk.gov.pay.connector.it.dao.DatabaseFixtures;
import uk.gov.pay.connector.junit.DropwizardConfig;
import uk.gov.pay.connector.junit.DropwizardJUnitRunner;
import uk.gov.pay.connector.junit.DropwizardTestContext;
import uk.gov.pay.connector.junit.TestContext;
import uk.gov.pay.connector.paritycheck.LedgerService;
import uk.gov.pay.connector.util.DatabaseTestHelper;

import java.time.ZonedDateTime;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static java.lang.String.format;
import static org.apache.commons.lang.math.RandomUtils.nextLong;
import static org.mockito.Mockito.mock;
import static uk.gov.pay.connector.junit.DropwizardJUnitRunner.WIREMOCK_PORT;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = ConnectorApp.class, config = "config/test-it-config.yaml")
public class ExpungeResourceIT {
    
    @ClassRule
    public static WireMockRule wireMockRule = new WireMockRule(WIREMOCK_PORT);
    
    @DropwizardTestContext
    protected TestContext testContext;
    
    private DatabaseFixtures.TestCharge testCharge;
    private DatabaseTestHelper databaseTestHelper;
    private DatabaseFixtures.TestAccount defaultTestAccount;
    
    private LedgerStub ledgerStub;


    @Before
    public void setUp() {
        ledgerStub = new LedgerStub();
        databaseTestHelper = testContext.getDatabaseTestHelper();
        insertTestAccount();
        testCharge = DatabaseFixtures.withDatabaseTestHelper(databaseTestHelper)
                .aTestCharge()
                .withChargeId(1L)
                .withCreatedDate(ZonedDateTime.now().minusDays(91))
                .withTestAccount(defaultTestAccount)
                .withExternalChargeId("external_charge_id")
                .withAmount(2500)
                .withChargeStatus(ChargeStatus.CAPTURED)
                .insert();
        ledgerStub.returnLedgerTransaction("external_charge_id");
    }

    private void insertTestAccount() {
        this.defaultTestAccount = DatabaseFixtures
                .withDatabaseTestHelper(databaseTestHelper)
                .aTestAccount()
                .withAccountId(nextLong())
                .insert();
    }


    @Test
    public void shouldExpireChargesBeforeAndAfterAuthorisationAndShouldHaveTheRightEvents() {

        var charge = databaseTestHelper.getChargeByExternalId("external_charge_id");

        System.out.println(format("Pre-charge: %s", charge));

        given().port(testContext.getPort())
                .contentType(JSON)
                .post("/v1/tasks/expunge")
                .then()
                .statusCode(200);

        var postCharge = databaseTestHelper.getChargeByExternalId("external_charge_id");

        System.out.println(format("Post-charge: %s", postCharge));
        // TODO: in PP-6098 
        // insert charges and assert that the charge has been expunged or marked as parity checked
    }

}
