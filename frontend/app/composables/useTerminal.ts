import Filter from 'ansi-to-html'

export const useTerminal = () => {
    const converter = new Filter({
        newline: true,
        escapeXML: true,
        colors: {
            0: '#000000', 1: '#EF4444', 2: '#22C55E', 3: '#EAB308',
            4: '#3B82F6', 5: '#A855F7', 6: '#06B6D4', 7: '#E2E8F0',
            8: '#475569', 9: '#F87171', 10: '#4ADE80', 11: '#FACC15',
            12: '#60A5FA', 13: '#C084FC', 14: '#22D3EE', 15: '#FFFFFF'
        }
    })

    const formatOutput = (text: string): string => {
        // Convert ANSI to HTML
        let html = converter.toHtml(text)

        // Replace newlines with <br> if not handled by converter
        html = html.replace(/\n/g, '<br/>')

        return html
    }

    return {
        formatOutput
    }
}
