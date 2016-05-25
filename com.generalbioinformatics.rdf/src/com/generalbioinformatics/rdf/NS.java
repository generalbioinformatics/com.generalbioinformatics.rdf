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
		
	public static String idAtc = "http://identifiers.org/atc/";
	
	public static String idBindingDB = "http://identifiers.org/bindingDB/";
	public static String idBiocyc = "http://identifiers.org/biocyc/";
	
 	public static String idCandida = "http://identifiers.org/cgd/";
	public static String idCas = "http://identifiers.org/cas/";
	public static String idCathSuperfamily = "http://identifiers.org/cath.superfamily/";
	public static String idCcds = "http://identifiers.org/ccds/";
	@Deprecated /** use idChebi2 */
	public static String idChebi = "http://identifiers.org/obo.chebi/";
	public static String idChebi2 = "http://identifiers.org/chebi/";
	public static String idChemblCompound = "http://identifiers.org/chembl.compound/";
	public static String idChemblTarget = "http://identifiers.org/chembl.target/";
	
	public static String idDbSnp = "http://identifiers.org/dbsnp/";
	public static String idDictybaseGene = "http://identifiers.org/dictybase.gene/";
	public static String idDoid = "http://identifiers.org/doid/";
	public static String idDrugbank = "http://identifiers.org/drugbank/";
	public static String idDrugbankTarget = "http://identifiers.org/drugbank.target/";
	
	@Deprecated /** use idNcbigene */
	public static String idEntrez = "http://identifiers.org/ncbigene/";
	public static String idEC = "http://identifiers.org/ec-code/";
	public static String idEnsembl = "http://identifiers.org/ensembl/";
	public static String idEnsemblBacteria = "http://identifiers.org/ensembl.bacteria/";
	public static String idEnsemblFungi = "http://identifiers.org/ensembl.fungi/";
	public static String idEnsemblPlant = "http://identifiers.org/ensembl.plant/";
	public static String idEnsemblProtists = "http://identifiers.org/ensembl.protist/";
	public static String idEnsemblMetazoa = "http://identifiers.org/ensembl.metazoa/";

	public static String idFlybase = "http://identifiers.org/flybase/";
	
	public static String idGenpept = "http://identifiers.org/genpept/";
	public static String idGo = "http://identifiers.org/go/";
	
	public static String idHomologene = "http://identifiers.org/homologene/";
	public static String idHprd = "http://identifiers.org/hprd/";
	@Deprecated /** use idHgncAcc or idHgncSymbol for clarity */
	public static String idHgnc = "http://identifiers.org/hgnc.symbol/";
	public static String idHgncAcc = "http://identifiers.org/hgnc/";
	public static String idHgncSymbol = "http://identifiers.org/hgnc.symbol/";
	
	public static String idIntact = "http://identifiers.org/intact/";
	public static String idInterpro = "http://identifiers.org/interpro/";
	public static String idInsdc = "http://identifiers.org/insdc/";
	public static String idIpi = "http://identifiers.org/ipi/";
	public static String idIsbn = "http://identifiers.org/isbn/";
	
	public static String idKeggCompound = "http://identifiers.org/kegg.compound/";
	public static String idKeggDrug = "http://identifiers.org/kegg.drug/";
	public static String idKeggPathway = "http://identifiers.org/kegg.pathway/";

	public static String idMesh = "http://identifiers.org/mesh/";
	
	@Deprecated /** use idMgd */
	public static String idMgi = "http://identifiers.org/mgd/";
	public static String idMgd = "http://identifiers.org/mgd/";
	public static String idMeddra = "http://identifiers.org/meddra/";
	public static String idMiriam = "http://identifiers.org/miriam.collection/";
	
	public static String idNdc = "http://identifiers.org/ndc/";
	public static String idNcbigene = "http://identifiers.org/ncbigene/";
	public static String idNcbiGi = "http://identifiers.org/ncbigi/";
	
	public static String idOmim = "http://identifiers.org/omim/";
	public static String idOrphanet = "http://identifiers.org/orphanet/";
	
	public static String idPfam = "http://identifiers.org/pfam/";
	public static String idPdb = "http://identifiers.org/pdb/";
	public static String idPsiMi = "http://identifiers.org/psimi/";
	public static String idPubmed= "http://identifiers.org/pubmed/";
	public static String idPantherPathway = "http://identifiers.org/panther.pathway/";
	public static String idPharmgkbDisease = "http://identifiers.org/pharmgkb.disease/";
	public static String idPharmgkbDrug = "http://identifiers.org/pharmgkb.drug/";
	public static String idPharmgkbPathway = "http://identifiers.org/pharmgkb.pathways/";
 	public static String idPharmgkbGene = "http://identifiers.org/pharmgkb.gene/";
	public static String idPirsf = "http://identifiers.org/pirsf/";
	public static String idProsite = "http://identifiers.org/prosite/";
	public static String idPubchemCompound = "http://identifiers.org/pubchem.compound/";
	public static String idPubchemSubstance = "http://identifiers.org/pubchem.substance/";
	
	public static String idRgd = "http://identifiers.org/rgd/";
	
	public static String idSnomed = "http://identifiers.org/snomedct/";
	public static String idSiderDrug = "http://identifiers.org/sider.drug/";
	public static String idSiderEffect = "http://identifiers.org/sider.effect/";
	public static String idStitch = "http://identifiers.org/stitch/";
	public static String idSupfam = "http://identifiers.org/supfam/";
		
	public static String idReactome = "http://identifiers.org/reactome/";
	public static String idRefseq = "http://identifiers.org/refseq/";
	
	public static String idTax = "http://identifiers.org/taxonomy/";
	
	public static String idUniprot = "http://identifiers.org/uniprot/";
	public static String idUnigene = "http://identifiers.org/unigene/";
	public static String idUniparc = "http://identifiers.org/uniparc/";
	public static String idUniprotIsoform = "http://identifiers.org/uniprot.isoform/";
	public static String idUnists = "http://identifiers.org/unists/";

	public static String idWormbase = "http://identifiers.org/wormbase/";
	public static String idWikipathways = "http://identifiers.org/wikipathways/";
	
	public static String OBO = "http://purl.obolibrary.org/obo#";
	public static String CHEBI = "http://purl.obolibrary.org/obo/chebi#";
	public static String CL = "http://purl.obolibrary.org/obo/cl#";
	public static String HP = "http://purl.obolibrary.org/obo/hp#";
	public static String SYMP = "http://purl.obolibrary.org/obo/symp#";
	public static String UBERON = "http://purl.obolibrary.org/obo/uberon#";
		
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

		nsMap.put ("fly-gene", "http://openflydata.org/id/flybase/feature/");
		nsMap.put ("biocyc", "http://biocyc.org/biopax/biopax-level3#");
		nsMap.put ("fly-affy", "http://openflydata.org/id/flyatlas/affyid/");
				
		nsMap.put ("id-chembl-t", idChemblTarget);
		nsMap.put ("id-chembl-c", idChemblCompound);		
		nsMap.put ("id-chebi", idChebi2);
		
		nsMap.put ("id-dbsnp", idDbSnp);		
		nsMap.put ("id-dictybase-g", idDictybaseGene);
		nsMap.put ("id-drugbank", idDrugbank);
		nsMap.put ("id-drugbank-t", idDrugbankTarget);
		
		nsMap.put ("id-EC", idEC);
		nsMap.put ("id-ensembl", idEnsembl);
		nsMap.put ("id-ensembl-metazoa", idEnsemblMetazoa);		
		nsMap.put ("id-ensembl-bacteria", idEnsemblBacteria);
		nsMap.put ("id-ensembl-fungi", idEnsemblFungi);
		nsMap.put ("id-ensembl-plant", idEnsemblPlant);
		nsMap.put ("id-ensembl-protists", idEnsemblProtists);
		
		nsMap.put ("id-flybase", idFlybase);
		
		nsMap.put ("id-go", idGo);		
		nsMap.put ("id-genpept", idGenpept);
		
		nsMap.put ("id-homologene", idHomologene);
		nsMap.put ("id-hgnc-acc", idHgncAcc);
		nsMap.put ("id-hgnc", idHgncSymbol);
		nsMap.put ("id-hprd", idHprd);
		
		nsMap.put ("id-intact", idIntact);
		nsMap.put ("id-interpro", idInterpro);
		nsMap.put ("id-insdc", idInsdc);
		nsMap.put ("id-ipi", idIpi);
		nsMap.put ("id-isbn", idIsbn);
		
		nsMap.put ("id-kegg-cmpnd", idKeggCompound);
		nsMap.put ("id-kegg-drug", idKeggDrug);
		nsMap.put ("id-kegg-pwy", idKeggPathway);
		
		nsMap.put ("id-miriam", idMiriam);
		nsMap.put ("id-mesh", idMesh);
		nsMap.put ("id-mgd", idMgd);
		
		nsMap.put ("id-ncbigene", idNcbigene);
		nsMap.put ("id-ncbi-gi", idNcbiGi);
		nsMap.put ("id-ndc", idNdc);

		nsMap.put ("id-omim", idOmim);
		nsMap.put ("id-orphanet", idOrphanet);

		nsMap.put ("id-panther-pwy", idPantherPathway);
		nsMap.put ("id-pharmgkb-disease", idPharmgkbDisease);
		nsMap.put ("id-pharmgkb-drug", idPharmgkbDrug);
		nsMap.put ("id-pharmgkb-pwy", idPharmgkbPathway);
		nsMap.put ("id-pirsf", idPirsf);
		nsMap.put ("id-prosite", idProsite);
		nsMap.put ("id-psimi", idPsiMi);
		nsMap.put ("id-pubchem-cmpnd", idPubchemCompound);
		nsMap.put ("id-pubchem-subst", idPubchemSubstance);
		nsMap.put ("id-pfam", idPfam);
		nsMap.put ("id-pdb", idPdb);
		nsMap.put ("id-psimi", idPsiMi);
		nsMap.put ("id-pubmed", idPubmed);

		nsMap.put ("id-rgd", idRgd);		
		nsMap.put ("id-reactome", idReactome);
		nsMap.put ("id-refseq", idRefseq);
		
		nsMap.put ("id-snomed", idSnomed);
		nsMap.put ("id-sider-drug", idSiderDrug);
		nsMap.put ("id-sider-effect", idSiderEffect);
		nsMap.put ("id-stitch", idStitch);
		nsMap.put ("id-supfam", idSupfam);
		
		nsMap.put ("id-tax", idTax);
		
		nsMap.put ("id-unigene", idUnigene);
		nsMap.put ("id-uniparc", idUniparc);
		nsMap.put ("id-uniprot", idUniprot);
		nsMap.put ("id-uniprot-isoform", idUniprotIsoform);
		nsMap.put ("id-unists", idUnists);

		nsMap.put ("id-wormbase", idWormbase);
		nsMap.put ("id-wikipwy", idWikipathways);

		nsMap.put ("flyatlas", "http://purl.org/NET/flyatlas/schema#");
		nsMap.put ("flyanat", "http://purl.org/obo/owl/FBbt#");
		
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
		nsMap.put ("uniprot-voc", "http://purl.uniprot.org/core/");
		nsMap.put ("mgi-voc", "http://bio2rdf.org/mgi_vocabulary:");


	}

}
