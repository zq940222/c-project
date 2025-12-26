module.exports = {
  stylesheet: [],
  css: `
    body {
      font-family: "Microsoft YaHei", "SimHei", "PingFang SC", sans-serif;
      font-size: 12px;
      line-height: 1.6;
    }
    h1 { font-size: 24px; margin-top: 20px; page-break-after: avoid; }
    h2 { font-size: 20px; margin-top: 16px; page-break-after: avoid; }
    h3 { font-size: 16px; margin-top: 12px; page-break-after: avoid; }
    h4 { font-size: 14px; margin-top: 10px; page-break-after: avoid; }

    table {
      width: 100%;
      border-collapse: collapse;
      margin: 10px 0;
      font-size: 11px;
      page-break-inside: avoid;
    }
    th, td {
      border: 1px solid #ddd;
      padding: 6px 8px;
      text-align: left;
    }
    th {
      background-color: #f5f5f5;
      font-weight: bold;
    }

    pre, code {
      font-family: "Consolas", "Microsoft YaHei Mono", monospace;
      font-size: 10px;
      background-color: #f8f8f8;
      border-radius: 3px;
      page-break-inside: avoid;
    }
    pre {
      padding: 10px;
      overflow-x: auto;
      white-space: pre-wrap;
      word-wrap: break-word;
      border: 1px solid #e0e0e0;
    }
    code {
      padding: 2px 4px;
    }

    blockquote {
      border-left: 3px solid #1890ff;
      padding-left: 12px;
      margin: 10px 0;
      color: #555;
    }

    hr {
      border: none;
      border-top: 1px solid #ddd;
      margin: 20px 0;
    }

    ul, ol {
      padding-left: 20px;
    }
    li {
      margin: 4px 0;
    }

    p {
      margin: 8px 0;
      page-break-inside: avoid;
    }

    /* 避免分页问题 */
    h1, h2, h3, h4, h5, h6 {
      page-break-after: avoid;
    }
    table, pre, blockquote {
      page-break-inside: avoid;
    }
  `,
  body_class: [],
  marked_options: {},
  pdf_options: {
    format: "A4",
    margin: {
      top: "20mm",
      bottom: "20mm",
      left: "15mm",
      right: "15mm"
    },
    printBackground: true,
    displayHeaderFooter: true,
    headerTemplate: "<div></div>",
    footerTemplate: "<div style=\"font-size: 9px; text-align: center; width: 100%; color: #888;\"><span class=\"pageNumber\"></span> / <span class=\"totalPages\"></span></div>"
  },
  launch_options: {
    args: ["--no-sandbox", "--disable-setuid-sandbox"]
  }
};
