// $Id: EFetchParser.java,v 1.4 2012/02/24 20:12:27 grossb Exp $
//------------------------------------------------------------------------------
/** Copyright (c) 2009 Memorial Sloan-Kettering Cancer Center.
 **
 ** Code written by: Ethan Cerami, Benjamin Gross
 ** Authors: Ethan Cerami, Gary Bader, Benjamin Gross, Chris Sander
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** Memorial Sloan-Kettering Cancer Center
 ** has no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall
 ** Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** Memorial Sloan-Kettering Cancer Center
 ** has been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/
package org.mskcc.cbio.biogene.eutils;

// imports
import org.apache.log4j.Logger;

import org.jdom2.Element;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.StringReader;
import java.util.List;

import org.mskcc.cbio.biogene.schema.*;

public class EFetchParser {

	private static String DELIMITER = ":";
    private static Logger log = Logger.getLogger(EFetchParser.class);

	// root
	private static String ENTREZ_GENE_ELEMENT = "Entrezgene";

	// gene-ref constants
	private static String ENTREZ_GENE_GENE_ELEMENT = "Entrezgene_gene";
	private static String ENTREZ_GENE_GENE_REF_ELEMENT = "Gene-ref";
	private static String ENTREZ_GENE_GENE_REF_LOCUS_ELEMENT = "Gene-ref_locus";
	private static String ENTREZ_GENE_GENE_REF_LOCUS_TAG_ELEMENT = "Gene-ref_locus-tag";
	private static String ENTREZ_GENE_GENE_REF_MAPLOC_ELEMENT = "Gene-ref_maploc";
	private static String ENTREZ_GENE_GENE_REF_DESCRIPTION_ELEMENT = "Gene-ref_desc";
	private static String ENTREZ_GENE_GENE_REF_SYN_ELEMENT = "Gene-ref_syn";
	private static String ENTREZ_GENE_GENE_REF_DB_ELEMENT = "Gene-ref_db";
	private static String ENTREZ_GENE_GENE_REF_DB_TAG_ELEMENT = "Dbtag";
	private static String ENTREZ_GENE_GENE_REF_DB_TAG_DB_ELEMENT = "Dbtag_db";
	private static String ENTREZ_GENE_GENE_REF_DB_TAG_TAG_ELEMENT = "Dbtag_tag";
	private static String ENTREZ_GENE_GENE_REF_DB_TAG_TAG_OBJECT_ID_ELEMENT = "Object-id";
	private static String ENTREZ_GENE_GENE_REF_DB_TAG_TAG_OBJECT_ID_ID_ELEMENT = "Object-id_id";

	// gene-source constants
	private static String ENTREZ_GENE_SOURCE_ELEMENT = "Entrezgene_source";
	private static String ENTREZ_GENE_SOURCE_BIOSOURCE_ELEMENT = "BioSource";
	private static String ENTREZ_GENE_SOURCE_BIOSOURCE_SUBTYPE_ELEMENT = "BioSource_subtype";
	private static String ENTREZ_GENE_SOURCE_BIOSOURCE_SUBTYPE_SUBSOURCE_ELEMENT = "SubSource";
	private static String ENTREZ_GENE_SOURCE_BIOSOURCE_SUBTYPE_SUBSOURCE_NAME_ELEMENT = "SubSource_name";
	private static String ENTREZ_GENE_SOURCE_BIOSOURCE_ORG_ELEMENT = "BioSource_org";
	private static String ENTREZ_GENE_SOURCE_BIOSOURCE_ORG_REF_ELEMENT = "Org-ref";
	private static String ENTREZ_GENE_SOURCE_BIOSOURCE_ORG_REF_TAXNAME_ELEMENT = "Org-ref_taxname";

	// summary
	private static String ENTREZ_GENE_SUMMARY_ELEMENT = "Entrezgene_summary";

	// gene-prot
	private static String ENTREZ_GENE_PROT_ELEMENT = "Entrezgene_prot";
	private static String ENTREZ_GENE_PROT_REF_ELEMENT = "Prot-ref";
	private static String ENTREZ_GENE_PROT_REF_NAME_ELEMENT = "Prot-ref_name";

	// comments constants
	private static String ENTREZ_GENE_COMMENTS_ELEMENT = "Entrezgene_comments";
	private static String GENE_COMMENTARY_ELEMENT = "Gene-commentary";
	private static String GENE_COMMENTARY_TYPE_ELEMENT = "Gene-commentary_type";
	private static String GENE_COMMENTARY_TYPE_VALUE_ATTRIBUTE = "value";
	private static String GENE_COMMENTARY_TYPE_RIF_VALUE = "generif";
	private static String GENE_COMMENTARY_TEXT_ELEMENT = "Gene-commentary_text";
	private static String GENE_COMMENTARY_REFS_ELEMENT = "Gene-commentary_refs";
	private static String GENE_COMMENTARY_REFS_PUB_ELEMENT = "Pub";
	private static String GENE_COMMENTARY_REFS_PUB_PMID_ELEMENT = "Pub_pmid";
	private static String GENE_COMMENTARY_REFS_PUB_PMID_ID_ELEMENT = "PubMedId";

    /**
     * Parses XML returned from eutils.
	 *
	 * This routine returns a GeneInfo object for the given content or
	 * null if an exception is encountered.  Therefore, a "valid" GeneInfo is 
	 * returned even if all the desired content is not found in the given content.
     *
     * @param content String (xml document)
	 * @eturn GeneInfo
     */
    public static GeneInfo parse(String content) {

		try {
			GeneInfo toReturn = new GeneInfo();
			StringReader reader = new StringReader(content);
			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build(reader);
			Element root = document.getRootElement();
			Element entrezGene = root.getChild(ENTREZ_GENE_ELEMENT);
			if (entrezGene != null) {
				if (log.isDebugEnabled()) {
					log.debug("EFetchParser.parse(), entrez gene element found.");
				}
				EFetchParser.parseEntrezGene(entrezGene, toReturn);
				return toReturn;
			}
		}
		catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("ESearchParser.parseESearchResults(), parse error.");
			}
			return null;
		}

		// outta here
		return null;
	}

	/**
	 * Parses entrez gene element
	 *
	 * @param e Element
	 * @param toReturn GeneInfo
	 */
	private static void parseEntrezGene(Element entrezGene, GeneInfo toReturn) throws Exception {
		
		// get gene ref props
		Element entrezGeneGene = entrezGene.getChild(ENTREZ_GENE_GENE_ELEMENT);
		if (entrezGeneGene != null) {
			if (log.isDebugEnabled()) {
				log.debug("EFetchParser.parseEntrezGene(), entrez gene - " + ENTREZ_GENE_GENE_ELEMENT + " found.");
			}
			parseEntrezGeneGene(entrezGeneGene, toReturn);
		}

		// get gene source props
		Element entrezGeneSource = entrezGene.getChild(ENTREZ_GENE_SOURCE_ELEMENT);
		if (entrezGeneSource != null) {
			if (log.isDebugEnabled()) {
				log.debug("EFetchParser.parseEntrezGene(), entrez gene - " + ENTREZ_GENE_SOURCE_ELEMENT + " found.");
			}
			parseEntrezGeneSource(entrezGeneSource, toReturn);
		}

		// gene summary
		Element entrezGeneSummary = entrezGene.getChild(ENTREZ_GENE_SUMMARY_ELEMENT);
		if (entrezGeneSummary != null) {
			if (log.isDebugEnabled()) {
				log.debug("EFetchParser.parseEntrezGene(), entrez gene - " + ENTREZ_GENE_SUMMARY_ELEMENT + " found.");
			}
			toReturn.setGeneSummary(entrezGeneSummary.getTextNormalize());
		}

		// get gene prot props
		Element entrezGeneProt = entrezGene.getChild(ENTREZ_GENE_PROT_ELEMENT);
		if (entrezGeneProt != null) {
			if (log.isDebugEnabled()) {
				log.debug("EFetchParser.parseEntrezGene(), entrez gene - " + ENTREZ_GENE_PROT_ELEMENT + " found.");
			}
			parseEntrezGeneProt(entrezGeneProt, toReturn);
		}

		// get comments/rifs
		Element entrezGeneComments = entrezGene.getChild(ENTREZ_GENE_COMMENTS_ELEMENT);
		if (entrezGeneComments != null) {
			if (log.isDebugEnabled()) {
				log.debug("EFetchParser.parseEntrezGene(), entrez gene - " + ENTREZ_GENE_COMMENTS_ELEMENT + " found.");
			}
			parseEntrezGeneComments(entrezGeneComments, toReturn);
		}
	}

	/**
	 * Parses entrez gene source props
	 * 
	 * @param entrezGeneSource Element
	 * @param toReturn GeneInfo
	 */
	private static void parseEntrezGeneSource(Element entrezGeneSource, GeneInfo toReturn) throws Exception {

		Element entrezBioSource = entrezGeneSource.getChild(ENTREZ_GENE_SOURCE_BIOSOURCE_ELEMENT);

		if (entrezBioSource != null) {
			if (log.isDebugEnabled()) {
				log.debug("EFetchParser.parseEntrezGene(), entrez gene - " + ENTREZ_GENE_SOURCE_BIOSOURCE_ELEMENT + " found.");
			}

			// taxonomy
			Element bioSourceOrg = entrezBioSource.getChild(ENTREZ_GENE_SOURCE_BIOSOURCE_ORG_ELEMENT);
			if (bioSourceOrg != null) {
				if (log.isDebugEnabled()) {
					log.debug("EFetchParser.parseEntrezGene(), entrez gene - " + ENTREZ_GENE_SOURCE_BIOSOURCE_ORG_ELEMENT  + " found.");
				}
				Element orgRef = bioSourceOrg.getChild(ENTREZ_GENE_SOURCE_BIOSOURCE_ORG_REF_ELEMENT);
				if (orgRef != null) {
					if (log.isDebugEnabled()) {
						log.debug("EFetchParser.parseEntrezGene(), entrez gene - " + ENTREZ_GENE_SOURCE_BIOSOURCE_ORG_REF_ELEMENT  + " found.");
					}
					Element taxName = orgRef.getChild(ENTREZ_GENE_SOURCE_BIOSOURCE_ORG_REF_TAXNAME_ELEMENT);
					if (taxName != null) {
						if (log.isDebugEnabled()) {
							log.debug("EFetchParser.parseEntrezGene(), entrez gene - " + ENTREZ_GENE_SOURCE_BIOSOURCE_ORG_REF_TAXNAME_ELEMENT  + " found.");
						}
						toReturn.setGeneOrganism(taxName.getTextNormalize());
					}
				}
			}

			// chromosome
			Element bioSourceSubType = entrezBioSource.getChild(ENTREZ_GENE_SOURCE_BIOSOURCE_SUBTYPE_ELEMENT);
			if (log.isDebugEnabled()) {
				log.debug("EFetchParser.parseEntrezGene(), entrez gene - " + ENTREZ_GENE_SOURCE_BIOSOURCE_SUBTYPE_ELEMENT  + " found.");
			}
			if (bioSourceSubType != null) {
				Element subSource = bioSourceSubType.getChild(ENTREZ_GENE_SOURCE_BIOSOURCE_SUBTYPE_SUBSOURCE_ELEMENT);
				if (log.isDebugEnabled()) {
					log.debug("EFetchParser.parseEntrezGene(), entrez gene - " + ENTREZ_GENE_SOURCE_BIOSOURCE_SUBTYPE_SUBSOURCE_ELEMENT  + " found.");
				}
				if (subSource != null) {
					Element subSourceName = subSource.getChild(ENTREZ_GENE_SOURCE_BIOSOURCE_SUBTYPE_SUBSOURCE_NAME_ELEMENT);
					if (subSourceName != null) {
						if (log.isDebugEnabled()) {
							log.debug("EFetchParser.parseEntrezGene(), entrez gene - " + ENTREZ_GENE_SOURCE_BIOSOURCE_SUBTYPE_SUBSOURCE_NAME_ELEMENT  + " found.");
						}
						toReturn.setGeneChromosome(subSourceName.getTextNormalize());
					}
				}
			}
		}
	}

	/**
	 * Parses entrez gene gene props
	 * 
	 * @param entrezGeneGene Element
	 * @param toReturn GeneInfo
	 */
	private static void parseEntrezGeneGene(Element entrezGeneGene, GeneInfo toReturn) throws Exception {

		Element entrezGeneRef = entrezGeneGene.getChild(ENTREZ_GENE_GENE_REF_ELEMENT);

		if (entrezGeneRef != null) {
			if (log.isDebugEnabled()) {
				log.debug("EFetchParser.parseEntrezGeneGene(), entrez gene - " + ENTREZ_GENE_GENE_REF_ELEMENT + " found.");
			}

			// locus (symbol)
			Element e = entrezGeneRef.getChild(ENTREZ_GENE_GENE_REF_LOCUS_ELEMENT);
			if (e != null) {
				if (log.isDebugEnabled()) {
					log.debug("EFetchParser.parseEntrezGene(), entrez gene - " + ENTREZ_GENE_GENE_REF_LOCUS_ELEMENT + " found.");
				}
				toReturn.setGeneSymbol(e.getTextNormalize());
			}

			// locus tag
			e = entrezGeneRef.getChild(ENTREZ_GENE_GENE_REF_LOCUS_TAG_ELEMENT);
			if (e != null) {
				if (log.isDebugEnabled()) {
					log.debug("EFetchParser.parseEntrezGene(), entrez gene - " + ENTREZ_GENE_GENE_REF_LOCUS_TAG_ELEMENT + " found.");
				}
				toReturn.setGeneTag(e.getTextNormalize());
			}

			// maploc
			e = entrezGeneRef.getChild(ENTREZ_GENE_GENE_REF_MAPLOC_ELEMENT);
			if (e != null) {
				if (log.isDebugEnabled()) {
					log.debug("EFetchParser.parseEntrezGene(), entrez gene - " + ENTREZ_GENE_GENE_REF_MAPLOC_ELEMENT + " found.");
				}
				toReturn.setGeneLocation(e.getTextNormalize());
			}

			// description
			e = entrezGeneRef.getChild(ENTREZ_GENE_GENE_REF_DESCRIPTION_ELEMENT);
			if (e != null) {
				if (log.isDebugEnabled()) {
					log.debug("EFetchParser.parseEntrezGene(), entrez gene - " + ENTREZ_GENE_GENE_REF_DESCRIPTION_ELEMENT + " found.");
				}
				toReturn.setGeneDescription(e.getTextNormalize());
			}

			// aliases
			e = entrezGeneRef.getChild(ENTREZ_GENE_GENE_REF_SYN_ELEMENT);
			if (e != null) {
				if (log.isDebugEnabled()) {
					log.debug("EFetchParser.parseEntrezGene(), entrez gene - " + ENTREZ_GENE_GENE_REF_SYN_ELEMENT + " found.");
				}
				List<Element> aliases = e.getChildren();
				String aliasesStr = "";
				for (Element alias : aliases) {
					String curAlias = alias.getTextNormalize();
					if (curAlias != null && curAlias.length() > 0) {
						if (log.isDebugEnabled()) {
							log.debug("EFetchParser.parseEntrezGene(), entrez gene - adding alias: " + curAlias);
						}
						aliasesStr += curAlias + DELIMITER;
					}
				}
				if (aliasesStr.length() > 0) {
					toReturn.setGeneAliases(aliasesStr.substring(0, aliasesStr.length()-1));
				}
			}

			// mim
			Element entrezGeneRefDb = entrezGeneRef.getChild(ENTREZ_GENE_GENE_REF_DB_ELEMENT);
			if (entrezGeneRefDb != null) {
				if (log.isDebugEnabled()) {
					log.debug("EFetchParser.parseEntrezGene(), entrez gene - " + ENTREZ_GENE_GENE_REF_DB_ELEMENT + " found.");
				}
				List<Element> refDBTags = entrezGeneRefDb.getChildren(ENTREZ_GENE_GENE_REF_DB_TAG_ELEMENT);
				for (Element refDBTag : refDBTags) {
					Element dbTagDB = refDBTag.getChild(ENTREZ_GENE_GENE_REF_DB_TAG_DB_ELEMENT);
					if (dbTagDB.getTextNormalize().equals("MIM")) {
						Element dbTagTag = refDBTag.getChild(ENTREZ_GENE_GENE_REF_DB_TAG_TAG_ELEMENT);
						if (dbTagTag != null) {
							Element objectID = dbTagTag.getChild(ENTREZ_GENE_GENE_REF_DB_TAG_TAG_OBJECT_ID_ELEMENT);
							if (objectID != null) {
								Element objectIDID = objectID.getChild(ENTREZ_GENE_GENE_REF_DB_TAG_TAG_OBJECT_ID_ID_ELEMENT);
								if (objectIDID != null) {
									toReturn.setGeneMim(objectIDID.getTextNormalize());
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Parses entrez gene source props
	 * 
	 * @param entrezGeneSource Element
	 * @param toReturn GeneInfo
	 */
	private static void parseEntrezGeneProt(Element entrezGeneProt, GeneInfo toReturn) throws Exception {

		Element entrezGeneProtRef = entrezGeneProt.getChild(ENTREZ_GENE_PROT_REF_ELEMENT);

		if (entrezGeneProtRef != null) {
			if (log.isDebugEnabled()) {
				log.debug("EFetchParser.parseEntrezGene(), entrez gene - " + ENTREZ_GENE_PROT_REF_ELEMENT + " found.");
			}
			Element entrezGeneProtRefName = entrezGeneProtRef.getChild(ENTREZ_GENE_PROT_REF_NAME_ELEMENT);
			if (entrezGeneProtRefName != null) {
				if (log.isDebugEnabled()) {
					log.debug("EFetchParser.parseEntrezGene(), entrez gene - " + ENTREZ_GENE_PROT_REF_NAME_ELEMENT + " found.");
				}
				List<Element> designations = entrezGeneProtRefName.getChildren();
				String designationsStr = "";
				for (Element designation : designations) {
					String curDesignation = designation.getTextNormalize();
					if (curDesignation != null && curDesignation.length() > 0) {
						if (log.isDebugEnabled()) {
							log.debug("EFetchParser.parseEntrezGene(), entrez gene - adding designation: " + curDesignation);
						}
						designationsStr += curDesignation + DELIMITER;
					}
				}
				if (designationsStr.length() > 0) {
					toReturn.setGeneDesignations(designationsStr.substring(0, designationsStr.length()-1));
				}
			}
		}
	}

	/**
	 * Parses entrez gene comments element
	 * 
	 * @param e Element
	 * @param toReturn GeneInfo
	 */
	private static void parseEntrezGeneComments(Element entrezGeneComments, GeneInfo toReturn) throws Exception {

		List<Element> comments = entrezGeneComments.getChildren(GENE_COMMENTARY_ELEMENT);
		for (Element comment : comments) {
			// get Type
			Element commentType = comment.getChild(GENE_COMMENTARY_TYPE_ELEMENT);
			if (commentType != null) {
				String commentTypeValue = commentType.getAttributeValue(GENE_COMMENTARY_TYPE_VALUE_ATTRIBUTE);
				// if we have generif, process
				if (commentTypeValue != null && commentTypeValue.equals(GENE_COMMENTARY_TYPE_RIF_VALUE)) {
					if (log.isDebugEnabled()) {
						log.debug("EFetchParser.parseEntrezGeneComments(), we have gene rif to process.");
					}
					// create new GeneRIFType
					GeneRIF geneRIF = new GeneRIF();
					// get rif text
					Element commentText = comment.getChild(GENE_COMMENTARY_TEXT_ELEMENT);
					if (commentText != null) {
						geneRIF.setRif(commentText.getTextNormalize());
					}
					// get pubmed id
					Element commentRefs = comment.getChild(GENE_COMMENTARY_REFS_ELEMENT);
					if (commentRefs != null) {
						Element pub = commentRefs.getChild(GENE_COMMENTARY_REFS_PUB_ELEMENT);
						if (pub != null) {
							Element pmid = pub.getChild(GENE_COMMENTARY_REFS_PUB_PMID_ELEMENT);
							if (pmid != null) {
								Element id = pmid.getChild(GENE_COMMENTARY_REFS_PUB_PMID_ID_ELEMENT);
								if (id != null) {
									geneRIF.setPubmedId(Integer.valueOf(id.getTextNormalize()));
								}
							}
						}
					}
					if (geneRIF.getRif() != null && geneRIF.getRif().length() > 0 && geneRIF.getPubmedId() > 0) {
						if (log.isDebugEnabled()) {
							log.debug("EFetchParser.parseEntrezGeneComments(), adding gene rif to rif list.");
						}
						toReturn.getGeneRif().add(geneRIF);
					}
				}
			}
		}
	}
}