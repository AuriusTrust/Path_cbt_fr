package org.pathfinderfr.app.util;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.colors.PatternColor;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.Style;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.BorderCollapsePropertyValue;
import com.itextpdf.layout.property.BorderRadius;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;

import org.pathfinderfr.app.database.entity.Character;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.Skill;
import org.pathfinderfr.app.database.entity.Weapon;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.List;

public class CharacterPDF {

    private Character character;
    private List<DBEntity> skills;
    private List<Weapon> weapons;
    private static final int LOGO_WIDTH = 185;
    private static final int STATS_CELL_SPACING = 3;
    private static final int STATS_CELL_WIDTH = 22;
    private static final Style STYLE_CELL_DEFAULT;
    private static final Style STYLE_HEADER;
    private static final Style STYLE_TEXT;
    private static final Style STYLE_TEXT_TOTAL;
    private static final Style STYLE_LABEL_TOP;
    private static final Style STYLE_LABEL_BOTTOM;
    private PdfFont FONT_BOLD;

    private static final String TEXT_LONG_TEST = "Ceci est un vraiment long texte pour tester dans les champs qui font vraiment plus que 100 charactères de long";

    static {
        STYLE_CELL_DEFAULT = new Style().setFontSize(8);
        STYLE_HEADER = new Style().setFontSize(4).setFontColor(ColorConstants.DARK_GRAY);
        STYLE_TEXT = new Style().setFontSize(8);
        STYLE_TEXT_TOTAL = new Style().setFontSize(10);
        STYLE_LABEL_TOP = new Style().setFontSize(8);
        STYLE_LABEL_BOTTOM = new Style().setFontSize(4);
    }

    public CharacterPDF(Character character, List<DBEntity> skills, List<Weapon> weapons) {
        this.character = character;
        this.skills = skills;
        this.weapons = weapons;

        try {
            FONT_BOLD = PdfFontFactory.createRegisteredFont(StandardFonts.HELVETICA_BOLD);
        } catch (IOException e) {
            FONT_BOLD = null;
        }
    }

    public Cell createHeader(String text) {
        return createHeader(text,TextAlignment.CENTER,1,1);
    }

    public Cell createHeader(String text, TextAlignment align) {
        return createHeader(text, align, 1,1);
    }

    public Cell createHeader(String text, int rowspan, int colspan) {
        return createHeader(text, TextAlignment.CENTER, rowspan, colspan);
    }

    public Cell createHeader(String text, TextAlignment align, int rowspan, int colspan) {
        return new Cell(rowspan, colspan)
                .addStyle(STYLE_HEADER)
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.BOTTOM)
                .setMargin(0)
                .setPadding(0)
                .add(new Paragraph(text.toUpperCase())
                    .setTextAlignment(align).setFixedLeading(5).setPaddingTop(1));
    }

    public Paragraph createLabelTop(String text) {
        Paragraph p = (new Paragraph(text))
                .addStyle(STYLE_LABEL_TOP)
                .setFixedLeading(6)
                .setPaddingTop(2)
                .setTextAlignment(TextAlignment.CENTER);
        if(FONT_BOLD != null) {
            p.setFont(FONT_BOLD);
        }
        return p;
    }

    public Paragraph createLabelBottom(String text) {
        Paragraph p = new Paragraph(text)
                .addStyle(STYLE_LABEL_BOTTOM)
                .setFixedLeading(8)
                .setTextAlignment(TextAlignment.CENTER);
        return p;
    }

    public Cell createLabel(String text1, String text2) {
        return createLabel(text1, text2,1,1);
    }

    public Cell createLabel(String text1, String text2, int rowspan, int colspan) {
        return new Cell(rowspan, colspan)
                .setPaddingLeft(2)
                .setPaddingRight(2)
                .setPaddingTop(0)
                .setPaddingBottom(0)
                .setMargin(0)
                .setHorizontalAlignment(HorizontalAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBackgroundColor(ColorConstants.BLACK)
                .setFontColor(ColorConstants.WHITE)
                .add(createLabelTop(text1.toUpperCase()))
                .add(createLabelBottom(text2.toUpperCase()));
    }

    public Cell createValueCell(Integer value) {
        return createValueCell(value, false);
    }

    public Cell createValueCell(Integer value, boolean total) {
        return createValueCell(value == null ? "" : String.format("%d", value), total, 1, 1);
    }

    public Cell createValueCell(String value, boolean total, int rowspan, int colspan) {
        Cell c = new Cell(rowspan, colspan)
                .setPadding(0)
                .setMargin(0)
                .addStyle(total ? STYLE_TEXT_TOTAL : STYLE_CELL_DEFAULT)
                .setHorizontalAlignment(HorizontalAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        if(colspan == 1) {
            c.add((new Paragraph(value))
                    .setWidth(STATS_CELL_WIDTH)
                    .setTextAlignment(TextAlignment.CENTER));
        } else {
            c.add((new Paragraph(value))
                    .setTextAlignment(TextAlignment.CENTER));
        }
        if(total) {
            c.setFont(FONT_BOLD);
        }
        return c;
    }

    public Cell createBonusCell(Integer bonus) {
        return new Cell()
                .setPadding(0)
                .setMargin(0)
                .addStyle(STYLE_CELL_DEFAULT)
                .setHorizontalAlignment(HorizontalAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .add((new Paragraph(bonus == null ? "" : String.format("%+d", bonus)))
                        .setWidth(STATS_CELL_WIDTH)
                        .setTextAlignment(TextAlignment.CENTER));
    }

    public Table createSectionStats() {
        Table table = new Table(5);
        table.setFixedPosition(18, 615, 0);
        table.setBorderCollapse(BorderCollapsePropertyValue.SEPARATE);
        table.setVerticalBorderSpacing(STATS_CELL_SPACING);
        table.setHorizontalBorderSpacing(STATS_CELL_SPACING);
        table.addCell(createHeader("Caractéristique"));
        table.addCell(createHeader("Valeur"));
        table.addCell(createHeader("Modif."));
        table.addCell(createHeader("Valeur temporaire"));
        table.addCell(createHeader("mod. temporaire"));
        table.addCell(createLabel("FOR", "Force"));
        table.addCell(createValueCell(character.getStrength()));
        table.addCell(createBonusCell(character.getStrengthModif()));
        table.addCell(createBonusCell(null));
        table.addCell(createBonusCell(null));
        table.addCell(createLabel("DEX", "Dextérité"));
        table.addCell(createValueCell(character.getDexterity()));
        table.addCell(createBonusCell(character.getDexterityModif()));
        table.addCell(createBonusCell(null));
        table.addCell(createBonusCell(null));
        table.addCell(createLabel("CON", "Constitution"));
        table.addCell(createValueCell(character.getConstitution()));
        table.addCell(createBonusCell(character.getConstitutionModif()));
        table.addCell(createBonusCell(null));
        table.addCell(createBonusCell(null));
        table.addCell(createLabel("INT", "Intelligence"));
        table.addCell(createValueCell(character.getIntelligence()));
        table.addCell(createBonusCell(character.getIntelligenceModif()));
        table.addCell(createBonusCell(null));
        table.addCell(createBonusCell(null));
        table.addCell(createLabel("SAG", "Sagesse"));
        table.addCell(createValueCell(character.getWisdom()));
        table.addCell(createBonusCell(character.getWisdomModif()));
        table.addCell(createBonusCell(null));
        table.addCell(createBonusCell(null));
        table.addCell(createLabel("CHA", "Charisme"));
        table.addCell(createValueCell(character.getCharisma()));
        table.addCell(createBonusCell(character.getCharismaModif()));
        table.addCell(createBonusCell(null));
        table.addCell(createBonusCell(null));
        return table;
    }

    public Cell createInfo(String label, int colspan) {
        return new Cell(1, colspan)
                .setPadding(0)
                .setMargin(0)
                .addStyle(STYLE_HEADER)
                .setBorder(Border.NO_BORDER)
                .setMinWidth(colspan*40)
                .setHorizontalAlignment(HorizontalAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.TOP)
                .add((new Paragraph(label.toUpperCase()).setFixedLeading(5).setPaddingTop(1)));
    }

    public Cell createInfoText(String text, int colspan) {
        return createInfoText(text, colspan, TextAlignment.LEFT);
    }

    public Cell createInfoText(String text, int colspan, TextAlignment align) {
        return createInfoText(text, colspan, align, false);
    }

    public Cell createInfoText(String text, int colspan, TextAlignment align, boolean border) {
        Cell c = new Cell(1, colspan)
                .setPadding(0)
                .setPaddingTop(3)
                .setMargin(0)
                .addStyle(STYLE_TEXT)
                .setMinHeight(10)
                .setMinWidth(colspan*40)
                .setHorizontalAlignment(HorizontalAlignment.LEFT)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .add((new Paragraph(text).setTextAlignment(align).setFixedLeading(9)));
        if(!border) {
            c.setBorderLeft(Border.NO_BORDER);
            c.setBorderRight(Border.NO_BORDER);
            c.setBorderTop(Border.NO_BORDER);
        }
        return c;
    }

    /**
     * Returns the french translation for the alignment
     */
    public String alignment2Text(int alignment) {
        switch(alignment) {
            case Character.ALIGN_LG: return "LB";
            case Character.ALIGN_NG: return "NB";
            case Character.ALIGN_CG: return "CB";
            case Character.ALIGN_LN: return "LN";
            case Character.ALIGN_CN: return "CN";
            case Character.ALIGN_LE: return "LM";
            case Character.ALIGN_NE: return "NM";
            case Character.ALIGN_CE: return "CM";
            default: return "N";
        }
    }

    /**
     * Returns the french translation for the alignment
     */
    public String size2Text(int size) {
        switch(size) {
            case Character.SIZE_FINE: return "I";
            case Character.SIZE_DIMINUTIVE: return "Min";
            case Character.SIZE_TINY: return "TP";
            case Character.SIZE_SMALL: return "P";
            case Character.SIZE_LARGE_TALL: return "G";
            case Character.SIZE_LARGE_LONG: return "G";
            case Character.SIZE_HUGE_TALL: return "TG";
            case Character.SIZE_HUGE_LONG: return "TG";
            case Character.SIZE_GARG_TALL: return "Gig";
            case Character.SIZE_GARG_LONG: return "Gig";
            case Character.SIZE_COLO_TALL: return "C";
            case Character.SIZE_COLO_LONG: return "C";
            default: return "M";
        }
    }

    /**
     * Returns a string with max characters
     */
    public String stringMax(String str, int maxChars) {
        if(str == null) {
            return "-";
        } else if(str.length() <= maxChars) {
            return str;
        } else {
            return str.substring(0, maxChars) + "…";
        }
    }

    public Table createSectionInfos() {
        Table table = new Table(8);
        table.setFixedPosition(225, 760, 0);
        table.setBorderCollapse(BorderCollapsePropertyValue.SEPARATE);
        table.setVerticalBorderSpacing(0);
        table.setHorizontalBorderSpacing(STATS_CELL_SPACING);

        table.addCell(createInfoText(stringMax(character.getName(), 30), 3));
        table.addCell(createInfoText(alignment2Text(character.getAlignment()), 1));
        table.addCell(createInfoText(stringMax(character.getPlayer(), 35), 4));
        table.addCell(createInfo("Nom du personnage", 3));
        table.addCell(createInfo("Alignement", 1));
        table.addCell(createInfo("Joueur", 4));

        table.addCell(createInfoText(character.getClassNames(), 4));
        table.addCell(createInfoText(stringMax(character.getDivinity(), 18), 2));
        table.addCell(createInfoText(stringMax(character.getOrigin(), 18), 2));
        table.addCell(createInfo("Classe et niveau", 4));
        table.addCell(createInfo("Divinité", 2));
        table.addCell(createInfo("Origine", 2));

        table.addCell(createInfoText(character.getRaceName(), 1));
        table.addCell(createInfoText(size2Text(character.getSizeType()), 1));
        table.addCell(createInfoText(character.getSex() == Character.SEX_M ? "M" : "F", 1));
        table.addCell(createInfoText(String.format("%d ans", character.getAge()), 1));
        table.addCell(createInfoText(String.format("%d cm", character.getHeight()), 1));
        table.addCell(createInfoText(String.format("%d kg", character.getWeight()), 1));
        table.addCell(createInfoText(stringMax(character.getHair(), 8), 1));
        table.addCell(createInfoText(stringMax(character.getEyes(), 8), 1));
        table.addCell(createInfo("Race", 1));
        table.addCell(createInfo("Catégorie de taille", 1));
        table.addCell(createInfo("Sexe", 1));
        table.addCell(createInfo("Âge", 1));
        table.addCell(createInfo("Taille", 1));
        table.addCell(createInfo("Poids", 1));
        table.addCell(createInfo("Cheveux", 1));
        table.addCell(createInfo("Yeux", 1));
        return table;
    }

    /**
     * Returns the french translation for the alignment
     */
    public String flyManeuverability2Text(int man) {
        if(man == 0) {
            return "-";
        }
        switch(man) {
            case Character.SPEED_MANEUV_CLUMBSY: return "Déplorable";
            case Character.SPEED_MANEUV_POOR: return "Médiocre";
            case Character.SPEED_MANEUV_GOOD: return "Bonne";
            case Character.SPEED_MANEUV_PERFECT: return "Parfaite";
            default: return "Moyenne";
        }
    }

    public Cell createSpeed(String label1, String label2, String value1, String value2) {
        Cell c = new Cell(1, label1 == null ? 1 : 2)
                .setPadding(0)
                .setMargin(0)
                .setMinWidth(label1 == null ? 35 : 70)
                .setMinHeight(15)
                .setHorizontalAlignment(HorizontalAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.BOTTOM);
        if(label1 != null) {
            Table t = new Table(4).setWidth(70);
            if(value1 != null) {
                t.addCell(new Cell().setBorder(Border.NO_BORDER).addStyle(STYLE_TEXT).setPadding(0).setMargin(0).setWidth(20)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE).setTextAlignment(TextAlignment.RIGHT).add(new Paragraph(value1)));
            } else {
                t.addCell(new Cell().setBorder(Border.NO_BORDER).setPadding(0).setMargin(0));
            }
            if(label1 != null) {
                t.addCell(new Cell().setBorder(Border.NO_BORDER).addStyle(STYLE_HEADER).setPadding(0).setMargin(0).setWidth(15)
                        .setVerticalAlignment(VerticalAlignment.BOTTOM).setTextAlignment(TextAlignment.RIGHT).add(new Paragraph(label1)));
            } else {
                t.addCell(new Cell().setBorder(Border.NO_BORDER).setPadding(0).setMargin(0));
            }
            if(value2 != null) {
                t.addCell(new Cell().setBorder(Border.NO_BORDER).addStyle(STYLE_TEXT).setPadding(0).setMargin(0).setWidth(20)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE).setTextAlignment(TextAlignment.RIGHT).add(new Paragraph(value2)));
            } else {
                t.addCell(new Cell().setBorder(Border.NO_BORDER).setPadding(0).setMargin(0));
            }
            if(label2 != null) {
                t.addCell(new Cell().setBorder(Border.NO_BORDER).addStyle(STYLE_HEADER).setPadding(0).setMargin(0).setWidth(15)
                        .setVerticalAlignment(VerticalAlignment.BOTTOM).setTextAlignment(TextAlignment.RIGHT).add(new Paragraph(label2)));
            } else {
                t.addCell(new Cell().setBorder(Border.NO_BORDER).setPadding(0).setMargin(0));
            }
            c.add(t);
        }
        return c;
    }

    public Cell createCell(String label, TextAlignment align, int rowspan, int colspan, int minWidth, int minHeight) {
        Cell c = new Cell(rowspan, colspan)
                .setPadding(0)
                .setPaddingLeft(2)
                .setPaddingRight(2)
                .setMargin(0)
                .setMinWidth(minWidth)
                .setMinHeight(minHeight)
                .addStyle(STYLE_HEADER)
                .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                .setVerticalAlignment(VerticalAlignment.TOP)
                .add(new Paragraph(label.toUpperCase()).setTextAlignment(align).setFixedLeading(5).setPaddingTop(1));
        return c;
    }

    public String formatSpeed(float speed, String unit) {
        if(speed <= 0) {
            return "-";
        } else {
            return new DecimalFormat("#.#" + unit).format(speed);
        }
    }

    public Table createSectionSpeed() {
        Table table = new Table(6);
        table.setFixedPosition(310, 705, 0);
        table.setBorderCollapse(BorderCollapsePropertyValue.SEPARATE);
        table.setVerticalBorderSpacing(0);
        table.setHorizontalBorderSpacing(STATS_CELL_SPACING);
        table.addCell(createLabel("VD", "Déplacement"));
        table.addCell(createSpeed("Mètres", "Cases", String.valueOf(character.getSpeed()*1.5), String.valueOf(character.getSpeed())));
        table.addCell(createSpeed("Mètres", "Cases", String.valueOf(character.getSpeedWithArmor()*1.5), String.valueOf(character.getSpeedWithArmor())));
        table.addCell(createCell("Mod. Temporaires", TextAlignment.CENTER, 3, 1, 58, 0));
        table.addCell(createCell("", TextAlignment.CENTER, 1, 1, 0, 0).setBorder(Border.NO_BORDER));
        table.addCell(createCell("Vitesse de déplacement", TextAlignment.CENTER, 1, 2, 0, 0).setBorder(Border.NO_BORDER).setPaddingBottom(2));
        table.addCell(createCell("Avec armure", TextAlignment.CENTER, 1, 2, 0, 0).setBorder(Border.NO_BORDER).setPaddingBottom(2));
        table.addCell(createSpeed("Mètres", null,formatSpeed(character.getBaseSpeedFly(), ""),flyManeuverability2Text(character.getBaseSpeedManeuverability())));
        table.addCell(createInfoText(formatSpeed(character.getSpeedSwimming(), " m"),1, TextAlignment.CENTER, true).setMinWidth(0));
        table.addCell(createInfoText(formatSpeed(character.getSpeedClimbing(), " m"), 1, TextAlignment.CENTER, true).setMinWidth(0));
        table.addCell(createInfoText(formatSpeed(character.getBaseSpeedDig(), " m"), 1, TextAlignment.CENTER, true).setMinWidth(0));
        table.addCell(createCell("Vol", TextAlignment.LEFT, 1, 1, 0, 0).setBorder(Border.NO_BORDER));
        table.addCell(createCell("Manœuvr.", TextAlignment.RIGHT, 1, 1, 0, 0).setBorder(Border.NO_BORDER));
        table.addCell(createCell("Natation", TextAlignment.CENTER, 1, 1, 0, 0).setBorder(Border.NO_BORDER));
        table.addCell(createCell("Escalade", TextAlignment.CENTER, 1, 1, 0, 0).setBorder(Border.NO_BORDER));
        table.addCell(createCell("Creusement", TextAlignment.CENTER, 1, 1, 0, 0).setBorder(Border.NO_BORDER));
        return table;
    }

    public Paragraph createValue(String value) {
        return createValue(value, false);
    }

    public Paragraph createValue(String value, boolean total) {
        Paragraph p = new Paragraph(value)
                .addStyle(total ? STYLE_TEXT_TOTAL : STYLE_TEXT)
                .setPadding(0)
                .setMargin(0)
                .setTextAlignment(TextAlignment.CENTER)
                .setFixedLeading(8);
        if(total) {
            p.setFont(FONT_BOLD);
        }
        return p;
    }

    public Table createSectionHitpoints() {
        Table table = new Table(3);
        table.setFixedPosition(175, 651, 0);
        table.setBorderCollapse(BorderCollapsePropertyValue.SEPARATE);
        table.setVerticalBorderSpacing(0);
        table.setHorizontalBorderSpacing(STATS_CELL_SPACING);
        table.addCell(createLabel("PV", "Points de vie"));
        table.addCell(createCell("Total", TextAlignment.LEFT, 1, 1, 0, 0).add(createValue(String.valueOf(character.getHitpoints()),true)));
        table.addCell(createCell("RD", TextAlignment.LEFT, 1, 1, 0, 0));
        table.addCell(createCell("Blessures/Points de vie actuels", TextAlignment.LEFT, 1, 3, 0, 0).setBorder(Border.NO_BORDER).setPaddingTop(5));
        table.addCell(createCell("", TextAlignment.LEFT, 1, 3, 120, 40));
        table.addCell(createCell("Dégâts non-létaux", TextAlignment.LEFT, 1, 3, 0, 0).setBorder(Border.NO_BORDER).setPaddingTop(5));
        table.addCell(createCell("", TextAlignment.LEFT, 1, 3, 120, 20));
        return table;
    }

    public Table createSectionInitiative() {
        Table table = new Table(6);
        table.setFixedPosition(175, 607, 100);
        table.setBorderCollapse(BorderCollapsePropertyValue.SEPARATE);
        table.setVerticalBorderSpacing(0);
        table.setHorizontalBorderSpacing(STATS_CELL_SPACING);
        table.addCell(createLabel("INI", "Initiative").setMinWidth(28));
        table.addCell(createValueCell(character.getInitiative()));
        table.addCell(createCell("=", TextAlignment.CENTER, 1, 1, 0, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getDexterityModif()));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 0, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getAdditionalBonus(Character.MODIF_COMBAT_INI)));
        table.addCell(new Cell().setBorder(Border.NO_BORDER));
        table.addCell(createCell("Total", TextAlignment.CENTER, 1, 1, 0, 0).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().setBorder(Border.NO_BORDER));
        table.addCell(createCell("Mod de DEX", TextAlignment.CENTER, 1, 1, 0, 0).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().setBorder(Border.NO_BORDER));
        table.addCell(createCell("Mod divers", TextAlignment.CENTER, 1, 1, 0, 0).setBorder(Border.NO_BORDER));
        return table;
    }

    public Table createSectionArmorClass() {
        Table table = new Table(19);
        table.setFixedPosition(21, 550, 0);
        table.setBorderCollapse(BorderCollapsePropertyValue.SEPARATE);
        table.setVerticalBorderSpacing(0);
        table.setHorizontalBorderSpacing(0);
        table.addCell(createLabel("CA", "Classe d'armure").setMinWidth(41));
        table.addCell(new Cell().setBorder(Border.NO_BORDER).setMinWidth(3).setPadding(0).setMargin(0));
        table.addCell(createValueCell(character.getArmorClass(), true));
        table.addCell(createCell("=", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createValueCell(10).setPadding(0).setMinWidth(9).setBorder(Border.NO_BORDER));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getAdditionalBonus(Character.MODIF_COMBAT_AC_ARMOR)));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getAdditionalBonus(Character.MODIF_COMBAT_AC_SHIELD)));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getDexterityModif()));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getSizeModifierArmorClass()));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getAdditionalBonus(Character.MODIF_COMBAT_AC_NATURAL)));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getAdditionalBonus(Character.MODIF_COMBAT_AC_PARADE)));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getAdditionalBonus(Character.MODIF_COMBAT_AC)));
        table.addCell(new Cell().setBorder(Border.NO_BORDER));
        table.addCell(new Cell().setBorder(Border.NO_BORDER));
        table.addCell(createCell("Total", TextAlignment.CENTER, 1, 1, 0, 0).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().setBorder(Border.NO_BORDER));
        table.addCell(new Cell().setBorder(Border.NO_BORDER));
        table.addCell(new Cell().setBorder(Border.NO_BORDER));
        table.addCell(createCell("Bonus armure", TextAlignment.CENTER, 1, 1, 0, 0).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().setBorder(Border.NO_BORDER));
        table.addCell(createCell("Bonus bouclier", TextAlignment.CENTER, 1, 1, 0, 0).setBorder(Border.NO_BORDER).setPadding(0));
        table.addCell(new Cell().setBorder(Border.NO_BORDER));
        table.addCell(createCell("Mod. de DEX", TextAlignment.CENTER, 1, 1, 0, 0).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().setBorder(Border.NO_BORDER));
        table.addCell(createCell("Mod. de taille", TextAlignment.CENTER, 1, 1, 0, 0).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().setBorder(Border.NO_BORDER));
        table.addCell(createCell("Armure naturelle", TextAlignment.CENTER, 1, 1, 24, 0).setBorder(Border.NO_BORDER).setPadding(0));
        table.addCell(new Cell().setBorder(Border.NO_BORDER));
        table.addCell(createCell("Mod. de parade", TextAlignment.CENTER, 1, 1, 0, 0).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().setBorder(Border.NO_BORDER));
        table.addCell(createCell("Mod. divers", TextAlignment.CENTER, 1, 1, 0, 0).setBorder(Border.NO_BORDER));
        table.addCell(new Cell(1,19).setBorder(Border.NO_BORDER));
        table.addCell(createLabel("Contact", "Classe d'armure"));
        table.addCell(new Cell().setBorder(Border.NO_BORDER).setPadding(0).setMargin(0));
        table.addCell(createValueCell(character.getArmorClassContact()));
        table.addCell(new Cell().setBorder(Border.NO_BORDER).setPadding(0).setMargin(0));
        table.addCell(new Cell(1,7).setBorder(Border.NO_BORDER).setPadding(0).setVerticalAlignment(VerticalAlignment.MIDDLE).setBackgroundColor(ColorConstants.BLACK).add(createLabel("Pris au dépourvu", "Classe d'armure")));
        table.addCell(new Cell().setBorder(Border.NO_BORDER).setPadding(0).setMargin(0));
        table.addCell(createValueCell(character.getArmorClassFlatFooted()));
        table.addCell(new Cell().setBorder(Border.NO_BORDER).setPadding(0).setMargin(0));
        table.addCell(createCell("Mod.", TextAlignment.RIGHT, 1, 5, 0, 0));
        return table;
    }



    public Table createSectionResistances() {
        Table table = new Table(15);
        table.setFixedPosition(21, 465, 0);
        table.setBorderCollapse(BorderCollapsePropertyValue.SEPARATE);
        table.setVerticalBorderSpacing(0);
        table.setHorizontalBorderSpacing(0);

        table.addCell(createHeader("Jets de sauvegarde"));
        table.addCell(createHeader(""));
        table.addCell(createHeader("Total"));
        table.addCell(createHeader(""));
        table.addCell(createHeader("Bonus de base"));
        table.addCell(createHeader(""));
        table.addCell(createHeader("Mod. de charac"));
        table.addCell(createHeader(""));
        table.addCell(createHeader("Mod. magique"));
        table.addCell(createHeader(""));
        table.addCell(createHeader("Mod. divers"));
        table.addCell(createHeader(""));
        table.addCell(createHeader("Mod. temporaire"));
        table.addCell(new Cell(7,1).setBorder(Border.NO_BORDER));
        table.addCell(createCell("Mod.", TextAlignment.RIGHT, 7, 1, 55, 0));

        table.addCell(new Cell(1,13).setBorder(Border.NO_BORDER));

        table.addCell(createLabel("Réflexes", "(Dextérité)").setMinWidth(50));
        table.addCell(new Cell().setBorder(Border.NO_BORDER).setMinWidth(3).setPadding(0).setMargin(0));
        table.addCell(createValueCell(character.getSavingThrowsReflexesTotal(), true));
        table.addCell(createCell("=", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getSavingThrowsReflexes()));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getDexterityModif()));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getAdditionalBonus(Character.MODIF_SAVES_MAG_ALL)+character.getAdditionalBonus(Character.MODIF_SAVES_MAG_REF)));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getAdditionalBonus(Character.MODIF_SAVES_ALL)+character.getAdditionalBonus(Character.MODIF_SAVES_REF)));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(null));

        table.addCell(new Cell(1,13).setBorder(Border.NO_BORDER));

        table.addCell(createLabel("Vigueur", "(Constitution)").setMinWidth(50));
        table.addCell(new Cell().setBorder(Border.NO_BORDER).setMinWidth(3).setPadding(0).setMargin(0));
        table.addCell(createValueCell(character.getSavingThrowsFortitudeTotal(), true));
        table.addCell(createCell("=", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getSavingThrowsFortitude()));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getConstitutionModif()));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getAdditionalBonus(Character.MODIF_SAVES_MAG_ALL)+character.getAdditionalBonus(Character.MODIF_SAVES_MAG_FOR)));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getAdditionalBonus(Character.MODIF_SAVES_ALL)+character.getAdditionalBonus(Character.MODIF_SAVES_FOR)));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(null));

        table.addCell(new Cell(1,13).setBorder(Border.NO_BORDER));

        table.addCell(createLabel("Volonté", "(Sagesse)").setMinWidth(50));
        table.addCell(new Cell().setBorder(Border.NO_BORDER).setMinWidth(3).setPadding(0).setMargin(0));
        table.addCell(createValueCell(character.getSavingThrowsWillTotal(), true));
        table.addCell(createCell("=", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getSavingThrowsWill()));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getWisdomModif()));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getAdditionalBonus(Character.MODIF_SAVES_MAG_ALL)+character.getAdditionalBonus(Character.MODIF_SAVES_MAG_WIL)));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getAdditionalBonus(Character.MODIF_SAVES_ALL)+character.getAdditionalBonus(Character.MODIF_SAVES_WIL)));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(null));

        return table;
    }


    public Table createSectionAttackDefense() {
        Table table = new Table(18);
        table.setFixedPosition(21, 375, 0);
        table.setBorderCollapse(BorderCollapsePropertyValue.SEPARATE);
        table.setVerticalBorderSpacing(0);
        table.setHorizontalBorderSpacing(0);

        table.addCell(createLabel("BBA", "Bonus de base à l'attaque").setMinWidth(70));
        table.addCell(new Cell().setBorder(Border.NO_BORDER).setMinWidth(3).setPadding(0).setMargin(0));
        table.addCell(createValueCell(character.getBaseAttackBonusAsString(), false, 1, 7));
        table.addCell(new Cell().setBorder(Border.NO_BORDER).setMinWidth(3).setPadding(0).setMargin(0));
        table.addCell(createLabel("Résistance", "à la magie", 1, 6).setMinWidth(67));
        table.addCell(new Cell().setBorder(Border.NO_BORDER).setMinWidth(3).setPadding(0).setMargin(0));
        table.addCell(createValueCell(character.getMagicResistance(), false));

        table.addCell(new Cell(1,18).setBorder(Border.NO_BORDER));

        table.addCell(createLabel("BMO", "Bonus de manœuvre offensive").setMinWidth(70));
        table.addCell(new Cell().setBorder(Border.NO_BORDER).setMinWidth(3).setPadding(0).setMargin(0));
        table.addCell(createValueCell(character.getCombatManeuverBonus(), true));
        table.addCell(createCell("=", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getBaseAttackBonusBest()));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getStrengthModif()));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getSizeModifierManeuver()));
        table.addCell(new Cell().setBorder(Border.NO_BORDER).setMinWidth(3).setPadding(0).setMargin(0));
        table.addCell(createCell("Mod.", TextAlignment.RIGHT, 2, 8, 0, 0));

        table.addCell(createHeader(""));
        table.addCell(createHeader(""));
        table.addCell(createHeader("Total").setVerticalAlignment(VerticalAlignment.TOP));
        table.addCell(createHeader(""));
        table.addCell(createHeader("BBA").setVerticalAlignment(VerticalAlignment.TOP));
        table.addCell(createHeader(""));
        table.addCell(createHeader("Mod. de force"));
        table.addCell(createHeader(""));
        table.addCell(createHeader("Mod. de taille"));
        table.addCell(createHeader(""));

        table.addCell(new Cell(1,18).setBorder(Border.NO_BORDER));

        table.addCell(createLabel("DMD", "Degré de manœuvre défensive").setMinWidth(70));
        table.addCell(new Cell().setBorder(Border.NO_BORDER).setMinWidth(3).setPadding(0).setMargin(0));
        table.addCell(createValueCell(character.getCombatManeuverDefense(), true));
        table.addCell(createCell("=", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getBaseAttackBonusBest()));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getStrengthModif()));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getDexterityModif()));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createBonusCell(character.getSizeModifierManeuver()));
        table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(createValueCell(10).setPadding(0).setMinWidth(9).setBorder(Border.NO_BORDER));
        table.addCell(createHeader("").setMinWidth(35));
        table.addCell(createHeader(""));
        table.addCell(createHeader(""));
        table.addCell(createHeader(""));
        table.addCell(createHeader(""));

        table.addCell(createHeader(""));
        table.addCell(createHeader(""));
        table.addCell(createHeader("Total").setVerticalAlignment(VerticalAlignment.TOP));
        table.addCell(createHeader(""));
        table.addCell(createHeader("BBA").setVerticalAlignment(VerticalAlignment.TOP));
        table.addCell(createHeader(""));
        table.addCell(createHeader("Mod. de force"));
        table.addCell(createHeader(""));
        table.addCell(createHeader("Mod. de Dex"));
        table.addCell(createHeader(""));
        table.addCell(createHeader("Mod. de taille"));
        table.addCell(createHeader(""));

        return table;
    }


    public Table createSectionWeapon(int left, int bottom, int weaponIdx) {
        Weapon w = weapons != null && weapons.size() > weaponIdx ? weapons.get(weaponIdx) : null;
        Table table = new Table(5);
        table.setFixedPosition(left, bottom, 0);
        table.addCell(createLabel("Arme", "", 2,3)
                .setBorderTop(Border.NO_BORDER)
                .setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setBorderTopLeftRadius(new BorderRadius(5))
                .setBorderTopRightRadius(new BorderRadius(5)).setMinWidth(170));
        table.addCell(createHeader("").setMinHeight(5));
        table.addCell(createHeader(""));
        table.addCell(createLabel("","Bonus à l'attaque").setMinWidth(58));
        table.addCell(createLabel("","Critique").setMinWidth(45));

        table.addCell(createInfoText(w == null ? "" : w.getName(), 3, TextAlignment.LEFT, true).setMinHeight(15).setPaddingLeft(3));
        String attackBonus = w == null ? "" : (w.isRanged() ? character.getAttackBonusRangeAsString() : character.getAttackBonusMeleeAsString());
        table.addCell(createInfoText(attackBonus, 1, TextAlignment.CENTER, true));
        table.addCell(createInfoText(w == null ? "" : w.getCritical(), 1, TextAlignment.CENTER, true));
        table.addCell(createLabel("","Type").setWidth(20));
        table.addCell(createLabel("","Portée"));
        table.addCell(createLabel("","Munitions").setMinWidth(90));
        table.addCell(createLabel("","Dégâts", 1,2));
        table.addCell(createInfoText(w == null ? "" : w.getType(), 1, TextAlignment.CENTER, true).setMinHeight(15));
        table.addCell(createInfoText(w == null ? "" : w.getRangeInMeters(), 1, TextAlignment.CENTER, true));
        table.addCell(createInfoText(w == null ? "" : (w.isRanged() ? "" : "-"), 1, TextAlignment.CENTER, true));
        String damage = w == null ? "" : w.getDamageForSize(character.getSizeType());
        int damageBonus = w == null ? 0 : w.getDamageBonus(character.getStrengthModif());
        if(damageBonus != 0) {
            damage = String.format("%s %+d", damage, damageBonus);
        }
        table.addCell(createInfoText(damage, 2, TextAlignment.CENTER, true));

        return table;
    }

    public Cell createCheckBox(boolean checked) {
        return new Cell().setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE)
                .add(new Paragraph().setBackgroundColor(checked ? ColorConstants.BLACK: ColorConstants.WHITE)
                .setMinHeight(6).setMinWidth(6)
                .setBorder(new SolidBorder(1)));
    }

    public Table createSectionSkills() {
        Table table = new Table(9);
        table.setFixedPosition(313, 145, 0);
        table.setBorderCollapse(BorderCollapsePropertyValue.SEPARATE);
        table.setVerticalBorderSpacing(0);
        table.setHorizontalBorderSpacing(0);
        table.addCell(createLabel("Compétences", "", 1,9).setMinWidth(251).setMinHeight(12));
        table.addCell(createHeader("").setMinWidth(10));
        table.addCell(createHeader("Nom de la compétence",TextAlignment.LEFT).setMinWidth(120));
        table.addCell(createHeader("Bonus total"));
        table.addCell(createHeader("Mod. carac",1,2));
        table.addCell(createHeader(""));
        table.addCell(createHeader("Rang"));
        table.addCell(createHeader(""));
        table.addCell(createHeader("Mod. divers"));
        int idx = 0;
        Color gray =  new DeviceRgb(230, 230, 230);
        for(DBEntity skill : skills) {
            if(skill instanceof Skill) {
                Color backgnd = idx++ % 2 == 0 ? gray : ColorConstants.WHITE;
                Skill s = (Skill)skill;
                String name = s.getName() + ( s.requiresTraining() ? "*" : "" );
                name = name.replaceAll("Connaissances", "Conn." );
                table.addCell(createCheckBox(character.isClassSkill(s.getName())).setBackgroundColor(backgnd));
                table.addCell(createInfoText(name,1).setBorder(Border.NO_BORDER).setBackgroundColor(backgnd).setPaddingBottom(2).setPaddingLeft(1));
                table.addCell(createValueCell(character.getSkillTotalBonus(s),true).setBorder(Border.NO_BORDER).setBackgroundColor(backgnd));
                table.addCell(createInfoText(s.getAbilityId(),1, TextAlignment.CENTER).setBorder(Border.NO_BORDER).setBackgroundColor(backgnd).setMinWidth(20).setPaddingBottom(2));
                table.addCell(createValueCell(character.getSkillAbilityMod(s)).setBorder(Border.NO_BORDER).setBackgroundColor(backgnd));
                table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER).setBackgroundColor(backgnd));
                table.addCell(createValueCell(character.getSkillRank(s.getId())).setBorder(Border.NO_BORDER).setBackgroundColor(backgnd));
                table.addCell(createCell("+", TextAlignment.CENTER, 1, 1, 5, 0).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(0).setBorder(Border.NO_BORDER).setBackgroundColor(backgnd));
                table.addCell(createValueCell(character.getSkillModBonus(s)).setBorder(Border.NO_BORDER).setBackgroundColor(backgnd));
            }
        }
        return table;
    }


    public Table createSectionOthers() {
        Table table = new Table(1);
        table.setFixedPosition(313, 50, 0);
        table.setBorderCollapse(BorderCollapsePropertyValue.SEPARATE);
        table.setVerticalBorderSpacing(0);
        table.setHorizontalBorderSpacing(0);

        table.addCell(createInfoText(stringMax(character.getModifsAsString(), 200), 1).setMinWidth(256).setVerticalAlignment(VerticalAlignment.BOTTOM));
        table.addCell(createInfo("Modificateurs particuliers", 1));

        table.addCell(createInfoText(stringMax(character.getLanguages(), 200), 1).setMinWidth(256).setMinHeight(30).setVerticalAlignment(VerticalAlignment.BOTTOM));
        table.addCell(createInfo("Langues", 1));
        return table;
    }


    public void generatePDF(OutputStream output, ImageData logo) {
        PdfDocument pdf = new PdfDocument(new PdfWriter(output));
        Document document = new Document(pdf);
        document.setMargins(20, 20,20,20);

        // show logo
        document.add((new Image(logo)).setWidth(LOGO_WIDTH));
        document.add(new Paragraph("Feuille de Personnage")
                .addStyle(STYLE_CELL_DEFAULT)
               .setFixedPosition(70, 760, 100));

        document.add(createSectionStats());
        document.add(createSectionInfos());
        document.add(createSectionHitpoints());
        document.add(createSectionSpeed());
        document.add(createSectionInitiative());
        document.add(createSectionArmorClass());
        document.add(createSectionResistances());
        document.add(createSectionAttackDefense());
        document.add(createSectionWeapon(21,310, 0));
        document.add(createSectionWeapon(21,245, 1));
        document.add(createSectionWeapon(21,180, 2));
        document.add(createSectionWeapon(21,115, 3));
        document.add(createSectionWeapon(21,50, 4));
        document.add(createSectionSkills());
        document.add(createSectionOthers());

        document.close();
    }
}
