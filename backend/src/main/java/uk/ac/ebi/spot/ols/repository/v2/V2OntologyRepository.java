
package uk.ac.ebi.spot.ols.repository.v2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.ols.model.v2.V2Ontology;
import uk.ac.ebi.spot.ols.repository.neo4j.OlsNeo4jClient;
import uk.ac.ebi.spot.ols.repository.solr.Fuzziness;
import uk.ac.ebi.spot.ols.repository.solr.OlsFacetedResultsPage;
import uk.ac.ebi.spot.ols.repository.solr.OlsSolrQuery;
import uk.ac.ebi.spot.ols.repository.solr.OlsSolrClient;
import uk.ac.ebi.spot.ols.repository.Validation;
import uk.ac.ebi.spot.ols.repository.v2.helpers.V2DynamicFilterParser;
import uk.ac.ebi.spot.ols.repository.v2.helpers.V2SearchFieldsParser;

import java.io.IOException;
import java.util.Map;

@Component
public class V2OntologyRepository {

    @Autowired
    OlsSolrClient solrClient;

    @Autowired
    OlsNeo4jClient neo4jClient;


    public OlsFacetedResultsPage<V2Ontology> find(
            Pageable pageable, String lang, String search, String searchFields, String boostFields, Map<String,String> properties) throws IOException {

        Validation.validateLang(lang);

        if(search != null && searchFields == null) {
            searchFields = "label^100 definition";
        }

        OlsSolrQuery query = new OlsSolrQuery();
        query.addFilter("lang", lang, Fuzziness.EXACT);
        query.addFilter("type", "ontology", Fuzziness.EXACT);
        V2SearchFieldsParser.addSearchFieldsToQuery(query, searchFields);
        V2SearchFieldsParser.addSearchFieldsToQuery(query, boostFields);
        V2DynamicFilterParser.addDynamicFiltersToQuery(query, properties);
        query.setSearchText(search);

        return solrClient.searchSolrPaginated(query, pageable)
                .map(result -> new V2Ontology(result, lang));
    }

    public V2Ontology getById(String ontologyId, String lang) throws ResourceNotFoundException {

        Validation.validateOntologyId(ontologyId);
        Validation.validateLang(lang);

        OlsSolrQuery query = new OlsSolrQuery();
        query.addFilter("lang", lang, Fuzziness.EXACT);
        query.addFilter("type", "ontology", Fuzziness.EXACT);
        query.addFilter("ontologyId", ontologyId, Fuzziness.EXACT);

        return new V2Ontology(solrClient.getOne(query), lang);
    }


}



