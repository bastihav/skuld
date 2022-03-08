package de.skuld.web.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.skuld.radix.RadixMetaData;
import de.skuld.radix.disk.DiskBasedRadixTrie;
import de.skuld.radix.manager.RadixManager;
import de.skuld.util.ConfigurationHelper;
import de.skuld.web.model.InlineResponse200;
import de.skuld.web.model.ManagementTreesBody;
import de.skuld.web.model.MetaData;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import java.nio.file.Paths;
import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-02-22T14:08:42.430Z[GMT]")
@RestController
public class ManagementApiController implements ManagementApi {

    private static final Logger log = LoggerFactory.getLogger(ManagementApiController.class);

    private final HttpServletRequest request;
    private final RadixManager<DiskBasedRadixTrie> radixManager;

    @org.springframework.beans.factory.annotation.Autowired
    public ManagementApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.request = request;

        this.radixManager = RadixManager.getInstance(Paths.get(ConfigurationHelper.getConfig().getString("radix.root")));
    }

    public ResponseEntity<MetaData> createTree(@Parameter(in = ParameterIn.DEFAULT, description = "date", required=true, schema=@Schema()) @Valid @RequestBody ManagementTreesBody body) {
        String accept = request.getHeader("Accept");
        if (accept != null && (accept.contains("application/json") || accept.contains("*/*"))) {
            UUID uuid = radixManager.createNewDiskBasedRadixTrie(Date.from(Instant.parse(body.getDate())));
            RadixMetaData radixMetaData = radixManager.getTries().get(uuid).getMetaData();

            Thread creatorThread = new Thread(() -> {
                radixManager.generateTrie(uuid);
            });
            creatorThread.start();

            return new ResponseEntity<MetaData>(radixMetaData.toAPIMetaData(), HttpStatus.OK);
        }

        return new ResponseEntity<MetaData>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<Void> deleteTree(@NotNull @Parameter(in = ParameterIn.QUERY, description = "UUID of tree to delete" ,required=true,schema=@Schema()) @Valid @RequestParam(value = "uuid", required = true) String uuid) {
        String accept = request.getHeader("Accept");
        radixManager.deleteRadixTrie(UUID.fromString(uuid));

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<InlineResponse200> getSetUpdater(@Parameter(in = ParameterIn.QUERY, description = "Start/Stop the updater thread" ,schema=@Schema()) @Valid @RequestParam(value = "setRunning", required = false) Boolean setRunning) {
        String accept = request.getHeader("Accept");
        if (accept != null && (accept.contains("application/json") || accept.contains("*/*"))) {
            if (setRunning != null) {
                if (setRunning) radixManager.startUpdaterThread();
                if (!setRunning) radixManager.stopUpdaterThread();
            }
            InlineResponse200 inlineResponse200 = new InlineResponse200();
            inlineResponse200.setRunning(radixManager.isUpdaterThreadRunning());
            return new ResponseEntity<InlineResponse200>(inlineResponse200, HttpStatus.OK);
        }

        return new ResponseEntity<InlineResponse200>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<List<MetaData>> getTrees() {
        String accept = request.getHeader("Accept");
        if (accept != null && (accept.contains("application/json") || accept.contains("*/*"))) {
            List<MetaData> metaDataList = radixManager.getTries().values().stream().map(trie -> trie.getMetaData()).map(RadixMetaData::toAPIMetaData).collect(
                Collectors.toList());
            return new ResponseEntity<List<MetaData>>(metaDataList, HttpStatus.OK);
        }

        return new ResponseEntity<List<MetaData>>(HttpStatus.NOT_IMPLEMENTED);
    }

}
