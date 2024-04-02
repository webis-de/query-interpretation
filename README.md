# Query Interpretation

This repository contains the entity-based query interpretation approach implemented in Java. Find the associated query entity-linker in [this repository](https://github.com/webis-de/query-entity-linking).

### Run on TIRA



```shell
make tira-run CORPUS=disks45-nocr-trec-robust-2004-20230209-training
```

### Citation
Please cite the following work when using this query interpretation approach.

```bibtex
@InProceedings{kasturia:2022,
  author =                   {Vaibhav Kasturia and Marcel Gohsen and Matthias Hagen},
  booktitle =                {15th ACM International Conference on Web Search and Data Mining (WSDM 2022)},
  doi =                      {10.1145/3488560.3498532},
  month =                    feb,
  publisher =                {ACM},
  site =                     {Tempe, AZ, USA},
  title =                    {{Query Interpretations from Entity-Linked Segmentations}},
  url =                      {https://dl.acm.org/doi/10.1145/3488560.3498532},
  year =                     2022
}
```

If you use query interpretations with TIREx please also cite the following paper. 

```bibtex
@InProceedings{gohsen:2024,
  author =                {Marcel Gohsen and Benno Stein},
  booktitle =             {Proceedings of the first International Workshop on Open Web Search (WOWS 2024)},
  keywords =              {ir, information retrieval, nlp, natural language processing, query, query-understanding},
  month =                 mar,
  publisher =             {CEUR Workshop Proceedings},
  site =                  {Glasgow, Scotland},
  title =                 {{Integrating Query Interpretation Components into the Information Retrieval Experiment Platform}},
  year =                  2024
}
```
