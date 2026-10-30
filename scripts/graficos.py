#!/usr/bin/env python3
import csv
import os
import statistics
import sys

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

RAIZ = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BRUTO = os.path.join(RAIZ, "resultados", "tempos.csv")
RESUMO = os.path.join(RAIZ, "resultados", "resumo.csv")
FIGURAS = os.path.join(RAIZ, "resultados", "figuras")

ALGORITMOS = ["Bubble", "Selection", "Quick", "Merge"]
ENTRADAS = ["aleatorio", "ordenado", "invertido", "quase_ordenado", "repeticoes"]
ROTULO = {"aleatorio": "aleatório", "ordenado": "ordenado", "invertido": "invertido",
          "quase_ordenado": "quase\nordenado", "repeticoes": "muitas\nrepetições"}

QUADRATICOS = ["Bubble", "Selection"]
COR = {alg: ("#2a78d6" if alg in QUADRATICOS else "#1baf7a") for alg in ALGORITMOS}
MARCA = {"Bubble": "o", "Selection": "s", "Quick": "^", "Merge": "D"}

TINTA, TINTA2, GRADE = "#0b0b0b", "#52514e", "#dddddd"
plt.rcParams.update({
    "font.family": "sans-serif", "font.sans-serif": ["Liberation Sans", "DejaVu Sans"],
    "font.size": 9, "axes.edgecolor": TINTA2, "axes.labelcolor": TINTA,
    "axes.titlecolor": TINTA, "text.color": TINTA, "xtick.color": TINTA2,
    "ytick.color": TINTA2, "axes.spines.top": False, "axes.spines.right": False,
    "figure.facecolor": "white", "axes.facecolor": "white", "legend.frameon": False,
    "lines.linewidth": 2, "lines.markersize": 5,
})


def resumir():
    amostras = {}
    with open(BRUTO, newline="", encoding="utf-8") as f:
        for r in csv.DictReader(f):
            chave = (r["algoritmo"], r["versao"], int(r["threads"]), r["entrada"], int(r["n"]))
            amostras.setdefault(chave, []).append(float(r["tempo_ms"]))

    stats = {}
    for chave, ts in amostras.items():
        stats[chave] = {"amostras": len(ts), "media": statistics.fmean(ts),
                        "desvio": statistics.stdev(ts) if len(ts) > 1 else 0.0}
    for (alg, _versao, _t, entrada, n), s in stats.items():
        s["speedup"] = stats[(alg, "serial", 1, entrada, n)]["media"] / s["media"]

    with open(RESUMO, "w", newline="", encoding="utf-8") as f:
        w = csv.writer(f)
        w.writerow(["algoritmo", "versao", "threads", "entrada", "n",
                    "amostras", "media_ms", "desvio_ms", "speedup"])
        for chave in sorted(stats, key=lambda c: (ALGORITMOS.index(c[0]), c[4], c[3], c[1], c[2])):
            s = stats[chave]
            w.writerow([*chave, s["amostras"], f'{s["media"]:.4f}',
                        f'{s["desvio"]:.4f}', f'{s["speedup"]:.4f}'])
    return stats


def enfeitar(ax):
    ax.grid(axis="y", color=GRADE, linewidth=0.7)
    ax.set_axisbelow(True)


def threads_de(stats, alg, n):
    return sorted({k[2] for k in stats
                   if k[0] == alg and k[1] == "paralelo" and k[3] == "aleatorio" and k[4] == n})


def rotular_pontas(fig, ax, pontas):
    fig.canvas.draw()
    para_fracao = ax.transAxes.inverted()
    pontas = sorted(pontas, key=lambda p: p[1])
    alturas = []
    for _texto, y, _cor in pontas:
        altura = para_fracao.transform(ax.transData.transform((0, y)))[1]
        if alturas and altura - alturas[-1] < 0.07:
            altura = alturas[-1] + 0.07
        alturas.append(altura)
    for (texto, _y, cor), altura in zip(pontas, alturas):
        ax.annotate(texto, (1.02, altura), xycoords="axes fraction", color=cor,
                    fontsize=8, va="center")


def fig1_tempo_vs_n(stats, tamanhos):
    fig, ax = plt.subplots(figsize=(7.2, 3.6))
    pontas = []
    for alg in ALGORITMOS:
        ys = [stats[(alg, "serial", 1, "aleatorio", n)]["media"] for n in tamanhos]
        ax.plot(tamanhos, ys, color=COR[alg], marker=MARCA[alg], label=alg)
        pontas.append((alg, ys[-1], COR[alg]))
    ax.set(xscale="log", yscale="log", xlabel="n (elementos)",
           ylabel="tempo médio (ms)")
    ax.set_xticks(tamanhos, [f"{n:,}".replace(",", ".") for n in tamanhos])
    ax.set_xticks([], minor=True)
    ax.margins(x=0.08)
    enfeitar(ax)
    ax.legend(ncols=4, fontsize=8, loc="lower left", bbox_to_anchor=(0, 1.0))
    ax.set_title("Tempo da versão serial, entrada aleatória — azul: O(n²), verde: O(n log n)",
                 fontsize=9, loc="left", pad=24)
    fig.tight_layout(rect=(0, 0, 0.9, 1))
    rotular_pontas(fig, ax, pontas)
    fig.savefig(os.path.join(FIGURAS, "fig1_tempo_vs_n.png"), dpi=200)
    plt.close(fig)


def fig2_speedup(stats, maior):
    fig, axes = plt.subplots(2, 2, figsize=(7.2, 5.0))
    for ax, alg in zip(axes.flat, ALGORITMOS):
        ts = threads_de(stats, alg, maior)
        ys = [stats[(alg, "paralelo", t, "aleatorio", maior)]["speedup"] for t in ts]
        ax.plot(ts, ts, color="#b8b7b2", linewidth=1.2, linestyle="--", label="ideal (linear)")
        ax.plot(ts, ys, color=COR[alg], marker=MARCA[alg], label="medido")
        for t, y in zip(ts, ys):
            if t in (ts[0], ts[-1]) or y == max(ys):
                ha = {ts[0]: "left", ts[-1]: "right"}.get(t, "center")
                dx = {"left": -2, "right": 2}.get(ha, 0)
                ax.annotate(f"{y:.2f}×", (t, y), textcoords="offset points",
                            xytext=(dx, 7), ha=ha, fontsize=8)
        ax.axhline(1, color=TINTA2, linewidth=0.8)
        ax.set_title(alg, fontsize=9, loc="left")
        ax.set_xscale("log", base=2)
        ax.set_xticks(ts, [str(t) for t in ts])
        ax.set_ylim(0, max(max(ys), 1.0) * 1.45)
        ax.set(xlabel="threads", ylabel="speedup")
        enfeitar(ax)
    fig.legend(*axes[0, 0].get_legend_handles_labels(), ncols=2, fontsize=8,
               loc="upper right", bbox_to_anchor=(0.99, 1.0))
    fig.suptitle(f"Speedup em relação ao serial — n = {maior:,}".replace(",", "."),
                 fontsize=9.5, x=0.01, ha="left")
    fig.tight_layout(rect=(0, 0, 1, 0.96))
    fig.savefig(os.path.join(FIGURAS, "fig2_speedup.png"), dpi=200)
    plt.close(fig)


def fig3_entradas(stats, maior):
    fig, axes = plt.subplots(2, 2, figsize=(7.2, 5.0))
    largura = 0.38
    for ax, alg in zip(axes.flat, ALGORITMOS):
        serial = [stats[(alg, "serial", 1, e, maior)]["media"] for e in ENTRADAS]
        paralelo = [min(stats[k]["media"] for k in stats if k[0] == alg
                        and k[1] == "paralelo" and k[3] == e and k[4] == maior)
                    for e in ENTRADAS]
        xs = range(len(ENTRADAS))
        ax.bar([x - largura / 2 - 0.01 for x in xs], serial, largura,
               color=COR[alg], label="serial")
        ax.bar([x + largura / 2 + 0.01 for x in xs], paralelo, largura, color=COR[alg],
               alpha=0.42, edgecolor=COR[alg], linewidth=1.2, label="paralelo (melhor nº de threads)")
        for x, (a, b) in enumerate(zip(serial, paralelo)):
            r = a / b
            ax.annotate(f"{r:.2f}×" if r < 1 else f"{r:.1f}×", (x, max(a, b)),
                        textcoords="offset points", xytext=(0, 4), ha="center",
                        fontsize=7.5, color=TINTA2)
        ax.set_xticks(list(xs), [ROTULO[e] for e in ENTRADAS], fontsize=7.5)
        ax.set_title(alg, fontsize=9, loc="left")
        ax.set_ylabel("tempo médio (ms)")
        ax.margins(y=0.2)
        enfeitar(ax)
    fig.legend(*axes[0, 0].get_legend_handles_labels(), ncols=2, fontsize=8,
               loc="upper right", bbox_to_anchor=(0.99, 1.0))
    fig.suptitle(f"Efeito da natureza da entrada — n = {maior:,}".replace(",", "."),
                 fontsize=9.5, x=0.01, ha="left")
    fig.tight_layout(rect=(0, 0, 1, 0.96))
    fig.savefig(os.path.join(FIGURAS, "fig3_entradas.png"), dpi=200)
    plt.close(fig)


def main():
    if not os.path.exists(BRUTO):
        sys.exit(f"faltando {BRUTO} — rode ordenacao.Benchmark primeiro")
    os.makedirs(FIGURAS, exist_ok=True)
    stats = resumir()

    tamanhos = sorted({k[4] for k in stats})
    fig1_tempo_vs_n(stats, tamanhos)
    fig2_speedup(stats, tamanhos[-1])
    fig3_entradas(stats, tamanhos[-1])
    print("resumo:", RESUMO)
    print("figuras:", FIGURAS)


if __name__ == "__main__":
    main()
