package nl.knaw.dans.bagit;


import static org.apache.commons.io.FileUtils.copyDirectory;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.transfer.BagFetcher;
import gov.loc.repository.bagit.transfer.dest.FileSystemFileDestination;
import gov.loc.repository.bagit.transfer.fetch.HttpFetchProtocol;
import gov.loc.repository.bagit.utilities.SimpleResult;
import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;


public class IssueReportTest {
    private File templateBag = new File("src/test/resources/template-bag");
    private BagFactory bagFactory = new BagFactory();

    /**
     * Fetching a payload file. This is correct according to the specs and works in the library.
     *
     * @link https://tools.ietf.org/html/draft-kunze-bagit#section-2.2.3
     * @throws Exception
     */
    @Test
    public void fetchToPayloadOk() throws Exception {
        File bagFile = new File("target/fetchToPayloadOk");
        FileUtils.deleteDirectory(bagFile);
        copyDirectory(templateBag, bagFile);
        BagFetcher fetcher = new BagFetcher(bagFactory);
        fetcher.registerProtocol("https", new HttpFetchProtocol());
        Bag bag = bagFactory.createBag(bagFile, BagFactory.Version.V0_97, null);
        FileSystemFileDestination dest = new FileSystemFileDestination(bagFile);
        SimpleResult result = fetcher.fetch(bag, dest, false, true);
        Assert.assertTrue(result.isSuccess());
    }

    /**
     * Fetching a non-payload file. This is not allowed according to the specs, at least the specs
     * only mention payload files. In the library an error is reported because of the absence of the
     * checksum in the payload manifest, but the file has been fetched anyway.
     *
     * @throws Exception
     */
    @Test
    public void fetchNonPayloadFileShouldFail() throws Exception {
        File bagFile = new File("target/fetchNonPayloadFileShouldFail");
        FileUtils.deleteDirectory(bagFile);
        copyDirectory(templateBag, bagFile);

        // Adding an extra line to the fetch.txt, to attempt to fetch a non-payload file
        FileUtils.writeStringToFile(
                new File(bagFile, "fetch.txt"),
                "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d3/Wikipedia-logo-v2-nl.svg/135px-Wikipedia-logo-v2-nl.svg.png - metadata/wikipedia.png",
                true);
//        FileUtils.writeStringToFile(
//                new File(bagFile, "tagmanifest-md5.txt"),
//                "bae7aa47892753a1355770164d780dec  metadata/wikipedia.png",
//                true);


        BagFetcher fetcher = new BagFetcher(bagFactory);
        fetcher.registerProtocol("https", new HttpFetchProtocol());
        Bag bag = bagFactory.createBag(bagFile, BagFactory.Version.V0_97, null);
        FileSystemFileDestination dest = new FileSystemFileDestination(bagFile);

        // Depending on
        SimpleResult result = fetcher.fetch(bag, dest, false, true); // Returns failure, but fetches metadata/wikipedia.png anyway
        //SimpleResult result = fetcher.fetch(bag, dest, true, true); // Returns success, but does not fetch metadata/wikipedia.png anyway
        Assert.assertFalse(result.isSuccess());

        // However ...
        Assert.assertFalse(new File(bagFile, "metadata/wikipedia.png").exists());
    }



}
