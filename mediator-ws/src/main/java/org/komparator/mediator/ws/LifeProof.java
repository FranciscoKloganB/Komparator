package org.komparator.mediator.ws;

import java.util.TimerTask;
import org.komparator.mediator.ws.cli.MediatorClient;
import org.komparator.mediator.ws.cli.MediatorClientException;
import org.komparator.security.domain.SecurityManager;
import static org.komparator.mediator.ws.MediatorApp.getMaxWaits;
import javax.xml.ws.WebServiceException;

public class LifeProof extends TimerTask {
  private static final int MAX_WAITS = MediatorApp.getMaxWaits();
  private SecurityManager secM = SecurityManager.getInstance();
  private String currentTime, lastLifeProof;
  private MediatorEndpointManager endpoint;
  private MediatorClient secondaryMediatorClient;
  private int waits;
  private boolean isPrimaryMediator;

  public LifeProof(MediatorEndpointManager endpoint) throws MediatorClientException {
    this.endpoint = endpoint;
    this.isPrimaryMediator = endpoint.getMediatorStatus();
    this.waits = 0;
    if (isPrimaryMediator) {
      this.secondaryMediatorClient = new MediatorClient(endpoint.getSecondaryWsURL());
    } else {
      this.secondaryMediatorClient = null;
    }
  }

  /**
  * Returns the instance of secondaryMediatorClient, if it's not initialized it
  * will return null.
  */
  public MediatorClient getSecondaryMediatorClient() {
    return secondaryMediatorClient;
  }

  /**
  * If the class loader of this lifeproof is a primary mediator, it will send a
  * signal to the secondary mediator to let him know, that the primary server is
  * still running. Otherwise, if the class loader is a secondary mediator it will
  * periodically check if the primary server as given him any signal throughout
  * the last 5s.
  * If any one of the servers stops responding for whatever reason, the task is
  * cancelled.
  */
  @Override
  public void run() {
    if (isPrimaryMediator) {
      try {
        System.out.println("");
        System.out.println("Sending imAlive signal to secondary Mediator.");
        secondaryMediatorClient.imAlive();
      } catch (WebServiceException e) {
        if (++waits >= MAX_WAITS) {
          cancel();
          System.out.println("Secondary mediator is not responding. Canceled periodic imAlive signal...");
          System.out.println("");
        } else {
          int triesLeft = MAX_WAITS - waits;
          System.out.println("Could not find online backup mediator.");
          System.out.println("Cancelling signal routine in: " + triesLeft + " attempts...");
        }
      }
    } else {
        try {
          System.out.println("");
          System.out.println("Looking for imAlive signal to secondary Mediator.");
          currentTime = secM.generateTimeStamp();
          lastLifeProof = endpoint.getLastLifeProof();
          if (!secM.primaryMediatorAlive(lastLifeProof, currentTime)) {
            System.out.println("Primary mediator failed to give life proof witin the last 5s. Canceling imAlive task...");
            System.out.println("Delegating primary role to this mediator instance...");
            System.out.println("");
            becomePrimary();
          }
        } catch (NullPointerException nPE) {
          if (++waits >= MAX_WAITS) {
            becomePrimary();
          } else {
            int triesLeft = MAX_WAITS - waits;
            System.out.println("No signal recieved, primary mediator did not start up.");
            System.out.println("Delegating to primary status to this mediator in: " + triesLeft + " attempts...");
          }
        }
      }
  }

  private void becomePrimary() {
    try {
      cancel();
      endpoint.publishToUDDI();
      this.isPrimaryMediator = true;
      System.out.println("Role delegation was successful. This mediator is now running as a primary.");
      System.out.println("");
    }  catch (Exception e) {
      System.out.println("Could not publish this mediator as primary on UDDI. Critical failure.");
    }
  }
}
