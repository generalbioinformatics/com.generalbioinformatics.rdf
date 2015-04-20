/**
* Copyright (c) 2015 General Bioinformatics Limited
* Distributed under the GNU GPL v2. For full terms see the file LICENSE.
*/
package com.generalbioinformatics.rdf;

/**
 * Some common namespace definitions as Strings.
 */
public class NS extends SimpleNamespaceMap
{
	//#Namespaces
	public static String BP = "http://www.biopax.org/release/biopax-level3.owl#";
	public static String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static String RDFS = "http://www.w3.org/2000/01/rdf-schema#";
	public static String OWL = "http://www.w3.org/2002/07/owl#";
	public static String XSD = "http://www.w3.org/2001/XMLSchema#";
	public static String DC = "http://purl.org/dc/elements/1.1/";
	public static String DC_TERMS = "http://purl.org/dc/terms/";
	public static String SKOS = "http://www.w3.org/2004/02/skos/core#";
	public static String VOID = "http://rdfs.org/ns/void#";
	public static String FOAF = "http://xmlns.com/foaf/0.1/";
	public static String SD = "http://www.w3.org/ns/sparql-service-description#";
	
	@Deprecated /** use idChebi2 */
	public static String idChebi = "http://identifiers.org/obo.chebi/";
	public static String idChebi2 = "http://identifiers.org/chebi/";
	
	public static String idChemblCompound = "http://identifiers.org/chembl.compound/";
	public static String idChemblTarget = "http://identifiers.org/chembl.target/";
	public static String idDbSnp = "http://identifiers.org/dbsnp/";
	public static String idEC = "http://identifiers.org/ec-code/";
	public static String idEnsembl = "http://identifiers.org/ensembl/";
	public static String idEnsemblPlants = "http://identifiers.org/ensembl.plant/";
	public static String idEnsemblMetazoa = "http://identifiers.org/ensembl.metazoa/";
	public static String idEntrez = "http://identifiers.org/ncbigene/";
	public static String idFlybase = "http://identifiers.org/flybase/";
	public static String idGenPept = "http://identifiers.org/genpept/";
	public static String idGo = "http://identifiers.org/go/";
	
	@Deprecated /** use idHgncAcc or idHgncSymbol for clarity */
	public static String idHgnc = "http://identifiers.org/hgnc.symbol/";
	public static String idHgncAcc = "http://identifiers.org/hgnc/";
	public static String idHgncSymbol = "http://identifiers.org/hgnc.symbol/";
	
	public static String idIntact = "http://identifiers.org/intact/";
	public static String idInterpro = "http://identifiers.org/interpro/";
	public static String idMgi = "http://identifiers.org/mgd/";
	public static String idMiriam = "http://identifiers.org/miriam.collection/";
	public static String idOmim = "http://identifiers.org/omim/";
	public static String idTax = "http://identifiers.org/taxonomy/";
	public static String idPfam = "http://identifiers.org/pfam/";
	public static String idPdb = "http://identifiers.org/pdb/";
	public static String idPsiMi = "http://identifiers.org/psimi/";
	public static String idPubmed= "http://identifiers.org/pubmed/";
	public static String idReactome = "http://identifiers.org/reactome/";
	public static String idRefseq = "http://identifiers.org/refseq/";
	public static String idSnomed = "http://identifiers.org/snomedct/";
	public static String idSuperfam = "http://identifiers.org/supfam/";
	public static String idUniprot = "http://identifiers.org/uniprot/";
	public static String idWormbase = "http://identifiers.org/wormbase/";
	public static String idWikipathways = "http://identifiers.org/wikipathways/";
	public static String idOrphanet = "http://identifiers.org/orphanet/";
	public static String idNdc = "http://identifiers.org/ndc/";
		
	public NS()
	{
		// hardcoded for now... //TODO: save to/from properties file
		
		nsMap.put ("bp", BP);
		nsMap.put ("skos", SKOS);
		nsMap.put ("rdf", RDF);
		nsMap.put ("rdfs", RDFS);
		nsMap.put ("dc", "http://purl.org/dc/terms/"); //TODO: mis-match definition of DC above.
		nsMap.put ("dc-elements", "http://purl.org/dc/elements/1.1/"); // TODO: what is difference between dc and dc-elements?
		nsMap.put ("void", VOID);
		nsMap.put ("foaf", FOAF);
		nsMap.put ("owl", OWL);
		nsMap.put ("xsd", XSD);

		nsMap.put ("gene", "http://openflydata.org/id/flybase/feature/");
		nsMap.put ("biocyc", "http://biocyc.org/biopax/biopax-level3#");
		nsMap.put ("affy", "http://openflydata.org/id/flyatlas/affyid/");
		nsMap.put ("u", idUniprot);
		nsMap.put ("t", idChemblTarget);
		nsMap.put ("c", idChemblCompound);
		nsMap.put ("flyatlas", "http://purl.org/NET/flyatlas/schema#");
		nsMap.put ("flyanat", "http://purl.org/obo/owl/FBbt#");
		nsMap.put ("reactome", idReactome);
		
		nsMap.put ("drugbank_voc", "http://bio2rdf.org/drugbank_vocabulary:");
		nsMap.put ("hgnc_voc", "http://bio2rdf.org/hgnc_vocabulary:");
		nsMap.put ("homologene_voc", "http://bio2rdf.org/homologene_vocabulary:");
		nsMap.put ("geneid_voc", "http://bio2rdf.org/geneid_vocabulary:");
		nsMap.put ("omim_voc", "http://bio2rdf.org/omim_vocabulary:");
		nsMap.put ("pharmgkb_voc", "http://bio2rdf.org/pharmgkb_vocabulary:");
		nsMap.put ("sgd_voc", "http://bio2rdf.org/sgd_vocabulary:");
		nsMap.put ("goa_voc", "http://bio2rdf.org/goa_vocabulary:");
		nsMap.put ("interpro_voc", "http://bio2rdf.org/interpro_vocabulary:");
		nsMap.put ("ctd_voc", "http://bio2rdf.org/ctd_vocabulary:");
		nsMap.put ("go_voc", "http://www.geneontology.org/dtds/go.dtd#");
		nsMap.put ("bodyatlas_voc", "http://marrs.generalbioinformatics.com/vocabulary/bodyatlas/");
		nsMap.put ("wormbase_voc", "http://generalbioinformatics.com/wormbase_voc#");
		nsMap.put ("marrs", "http://marrs.generalbioinformatics.com/");
		nsMap.put ("clinvar-voc", "http://generalbioinformatics.com/ontology/clinvar#");
		nsMap.put ("ortholog-voc", "http://generalbioinformatics.com/ontologies/orthologMapping#");
		nsMap.put ("uniprot-voc", "http://purl.uniprot.org/core/");
		nsMap.put ("taxonomy-voc", "http://generalbioinformatics.com/ontology/taxonomy#");
		nsMap.put ("hapmap-voc", "http://www.generalbioinformatics.com/ontologies/hapmap#");
		nsMap.put ("mgi-voc", "http://bio2rdf.org/mgi_vocabulary:");
		nsMap.put ("pdb-voc", "http://generalbioinformatics.com/ontologies/2013/pdb#");
		nsMap.put ("nhgri-voc", "http://generalbioinformatics.com/ontologies/nhgri#");
		
		// TODO -> check with Nadia
		nsMap.put ("GB", "http://generalbioinformatics.com/ontology#");

	}

}
